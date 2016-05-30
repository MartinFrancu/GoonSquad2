package ctfbot;

import cz.cuni.amis.pogamut.sposh.ut2004.StateSposhLogicController;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.bot.IUT2004BotController;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import java.io.IOException;
import java.util.logging.Level;

/**
 * yaPOSH logic for CTF bot.
 * 
 * @see IUT2004BotController Controller for various methods called during construction of bot.
 * 
 * @author Jimmy
 */
public class CTFBotLogic extends StateSposhLogicController<UT2004Bot, CTFBotContext> {

   // private String SPOSH_PLAN_RESOURCE = "sposh/plan/bot.lap";
    private String SPOSH_PLAN_RESOURCE = "sposh/plan/SimplePlan.lap";
	//private String SPOSH_PLAN_RESOURCE = "sposh/plan/ctfbot-example.lap";

    @Override
    protected String getPlan() throws IOException {
        return getPlanFromResource(SPOSH_PLAN_RESOURCE);                
    }

    /**
     * Create context that can be accessed in every state primitive.
     *
     * @return new context of this bot.
     */
    @Override
    protected CTFBotContext createContext() {
        return new CTFBotContext(bot);
    }

    static int botCount = 0;
    
    /**
     * Initialize command of the bot, called during initial handshake, init can
     * set things like name of bot, its skin, skill, team ect.
     *
     * @see Initialize
     * @return
     */
    @Override
    public Initialize getInitializeCommand() {
        int team = (botCount++ % 2 == 0) ? AgentInfo.TEAM_BLUE : AgentInfo.TEAM_RED;
        String teamStr = (team == AgentInfo.TEAM_BLUE ? "BLUE" : "RED");
        return new Initialize().setName("CTFBot[" + teamStr + "]-" + (botCount-1)).setTeam(team);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        super.botInitialized(gameInfo, currentConfig, init);
        log.setLevel(Level.INFO);
        bot.getLogger().getCategory(SPOSH_LOG_CATEGORY).setLevel(Level.WARNING);
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);        
    }

    @Override
    public void botKilled(BotKilled event) {
        context.DealWithBotDeath();
    }

    @Override
    protected void logicBeforePlan() {
        getContext().logicBeforePlan();
        super.logicBeforePlan();
    }

    @Override
    protected void logicAfterPlan() {
        super.logicAfterPlan();
        getContext().logicAfterPlan();        
    }    

    public static void main(String[] args) throws PogamutException {
        new UT2004BotRunner(CTFBotLogic.class, "CTF-TC-Bot").setMain(true).setLogLevel(Level.WARNING).startAgents(6);
    }
    
}
