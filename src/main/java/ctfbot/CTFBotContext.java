package ctfbot;

import ctfbot.action.dm.ShootPlayer;
import ctfbot.tc.CTFCommItems;
import ctfbot.tc.CTFCommObjectUpdates;
import ctfbot.tc.GoonSquad.RoleMessage;
import ctfbot.tc.GoonSquad.TeamRoles;
import ctfbot.tc.GoonSquad.WhoIsWhoQuery;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.sposh.context.UT2004Context;
import cz.cuni.amis.pogamut.unreal.communication.messages.UnrealId;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.TabooSet;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.logging.Level;

/**
 * This serves as a template bot for creating simple CTF bot using YaPOSH.
 * 
 * Many primitives are implemented so it is easy to start design the behavior using graphical
 * YaPOSH editor only.
 * 
 * If you are in NetBeans, check Projects/ctfbot project/Other sources/bot.lap plan.
 * 
 * Contains support for Team Communication, be sure to {@link StartTeamComm} first! 
 *
 * @author Jimmy
 */
public class CTFBotContext extends UT2004Context<UT2004Bot> {
    
    
    ////////////////////////////////////////////////////////////////////////////
    /// TEAM ATRIBUTS:
    public boolean reportedIn = false;
    public Player head = null;
    public int otherDefendersCount = 0;
    public TeamRoles teamRole = TeamRoles.DEFENDER; // three values: DEFENDER, HEAD, TAIL, DONT FORGET TO STING

    public void setTeamRole(TeamRoles teamRoleIn) {
        switch(teamRoleIn)
        {
            case DEFENDER :
                teamRoleString = "DEFENDER";
                break;
            case HEAD :
                teamRoleString = "HEAD";
            case TAIL :
                teamRoleString = "TAIL";
        }   
        this.teamRole = teamRoleIn;
    }
    
    public String teamRoleString = "DEFENDER";
    
    
    ///////////////////////////////////////////////////////////////////////////
    // TEAM COMMUNICATION METHODS:
    
    public boolean SendWhoIsWhoQuery() // return false if it is unsuccesfull 
    {// sends to all
        if(tcClient.isConnected())
        {
            log.log(Level.SEVERE, "SENDING QUERY");
            tcClient.sendToTeamOthers(new WhoIsWhoQuery(getInfo().getId(), System.currentTimeMillis()));
            return true;
        }else
        {
            log.log(Level.SEVERE, "*******************************UNABLE TO SEND MESSAGE (SEND QUERY)");
            return false;
        }
    }
    public boolean ReportYourRole(UnrealId toWhom)
    {
        if(tcClient.isConnected())
        {
            log.log(Level.SEVERE, "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%SENDING ROLE REPORT TO: "+ toWhom);
            tcClient.sendToBot(toWhom, new RoleMessage(getInfo().getId(), teamRole,System.currentTimeMillis()));
            return true;
        }else
        {
            log.log(Level.SEVERE, "****************************UNABLE TO SEND MESSAGE (REPORT ROLE)");
            return false;
        }
    }
    @EventListener (eventClass = WhoIsWhoQuery.class)
    public void ReplyToWhoIsWhoQuery(WhoIsWhoQuery query)
    {
        // this query also means, new defender is here :
        log.log(Level.SEVERE, "###################################GOT QUERY FROM: "+ query.senderId);
        ReportYourRole(query.senderId);
    }
    
    
    public boolean TellEveryOneIamDefender()
    {///////// NOT USED
        if(tcClient.isConnected())
        {
            tcClient.sendToTeamOthers(new RoleMessage(getInfo().getId(), TeamRoles.DEFENDER, System.currentTimeMillis()) );
            log.log(Level.SEVERE, "SENDING EVERYONE I AM DEFENDER");
            return true;
        }else
        {
            log.log(Level.SEVERE, "*******************************UNABLE TO SEND MESSAGE (SEND DEFENDER INFO)");
            return false;
        }
             
    }
    
    public boolean IntroduceYourSelf()
    {
        if(tcClient.isConnected())
        {
            log.log(Level.SEVERE, "INTRODUCING MY SELF");
            tcClient.sendToTeamOthers(new WhoIsWhoQuery(getInfo().getId(), System.currentTimeMillis()));
            tcClient.sendToTeamOthers(new RoleMessage(getInfo().getId(), this.teamRole, System.currentTimeMillis()) );
            return true;
        }else
        {
            log.log(Level.SEVERE, "*******************************UNABLE TO SEND MESSAGE (INDTRODUCING MY SELF)");
            return false;
        }
    }
    
    @EventListener (eventClass = RoleMessage.class)
    public void AcknowledgeNewRole(RoleMessage msg)
    {
        log.log(Level.SEVERE, "ACKNOWLEDGING THE ROLE REPORT:" + msg.role);
        switch(msg.role)
        {
            case DEFENDER:
                // new defender:
                ++otherDefendersCount;
                break;
            case HEAD:
                this.head = getPlayers().getPlayer(msg.senderId);
                break;
            case TAIL:
                // dont care about tails
                break;
            
        }
        
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    
    /** Number of the logic iteration. **/
    public int logicIteration = 0;
    /** Time of the last logic... **/
    public long lastLogic = 0;    
    /** CTF Communication - World Object Updates **/
    public CTFCommObjectUpdates commObjectUpdates;
    /** CTF Communication - Choosing Items to Pick Up **/
    public CTFCommItems commItems;
    /** Current item our bot is currently going for */
    public Item targetItem = null;
    /** Target enemy player we are currently shooting at */
    public Player targetPlayer = null;    
    /** Used to taboo items we were stuck going for or we have picked up recently */
    public TabooSet<Item> tabooItems;
    
    public String state = "";

    public CTFBotContext(UT2004Bot bot) {
        super("CTFBotContext", bot);
        // IMPORTANT: Various modules of context must be initialized.
        initialize();
        
        // INITIALIZE TEAM COMM
        commObjectUpdates = new CTFCommObjectUpdates(this);
        commItems         = new CTFCommItems(this);
        
        // INITIALIZE CUSTOM MODULES
        tabooItems = new TabooSet<Item>(bot);
        
        this.getNavigation().addStrongNavigationListener(new FlagListener<NavigationState>() {
            @Override
            public void flagChanged(NavigationState changedValue) {
                switch (changedValue) {
                    case PATH_COMPUTATION_FAILED:
                    case STUCK:
                        if (targetItem != null)
                            tabooItems.add(targetItem, 30);
                    break;
                    case TARGET_REACHED:
                        if (targetItem != null)
                            tabooItems.add(targetItem, 5);                        
                    break;
                }
            }
        });

        // HERE IS A GOOD PLACE TO INITIALIZE YOUR WEAPON PREFERENCES
        
        this.getWeaponPrefs()
                .addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true)
                .addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true)
                .addGeneralPref(UT2004ItemType.MINIGUN, true)                
                .addGeneralPref(UT2004ItemType.LINK_GUN, true)
                .addGeneralPref(UT2004ItemType.FLAK_CANNON, true)                                
                .addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true); 
    }

    /**
     * Here we are marking if our bot "is shooting" or "is about to start shooting"; see {@link ShootPlayer} and alikes.
     */
    public boolean isShooting;
    
    /**
     * Here we are marking what weapon our bot currently wields in its hands or is about to be changed to.
     */
    public ItemType currentWeapon;
    
    /**
     * This method is invoked before yaPOSH engine evaluation.
     */
    void logicBeforePlan() {    	        
        if (lastLogic <= 0) {
        	log.warning("---[ LOGIC ITERATION " + (++logicIteration) + " ]---");
        	lastLogic = System.currentTimeMillis();
        } else {
        	long now = System.currentTimeMillis();
        	log.warning("---[ LOGIC ITERATION " + (++logicIteration) + " | " + (now - lastLogic) + " ms delta]---");
        	lastLogic = now;
        }
        log.warning("AAAAAAAAAAAAAAAAAAAAA ROLE: " + this.teamRoleString + " there are " + this.otherDefendersCount + " other defenders.");
        isShooting = info.isShooting();
        currentWeapon = weaponry.getCurrentWeapon().getType();
       
        if (info.getSelf() != null) {
        	commObjectUpdates.update();
        	commItems.update();
    	}
        ////////////////////////////////////////////
        /// try to report in to team if not reported yet:
        if(!reportedIn)
        {
            reportedIn = this.IntroduceYourSelf();
        }
        
        this.bot.getBotName().setTag(this.teamRoleString);
        ////////////////////////////////////////////
    }

    /**
     * This method is invoked after yaPOSH engine evaluation.
     */
    void logicAfterPlan() {       
    }
    

}
