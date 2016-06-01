package ctfbot.action.ctf;

import ctfbot.CTFBotContext;
import ctfbot.tc.GoonSquad.TeamRoles;
import cz.cuni.amis.pogamut.sposh.context.UT2004Context;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.ParamsAction;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;

/**
 * Action SetMyRole for Yaposh.
 *
 * @author student
 * @param <CONTEXT> Context class of the action. It's an shared object used by
 * all primitives. it is used as a shared memory and for interaction with the
 * environment.
 */
@PrimitiveInfo(name = "SetMyRole", description = "Description of SetMyRole")
public class SetMyRole<CONTEXT extends CTFBotContext> extends ParamsAction<CONTEXT> {

    /**
     * Constructor of the action, used during automatic instantiation.
     */
    public SetMyRole(CONTEXT ctx) {
        super(ctx);
    }

    /**
     * Method responsible for initialization of the action. The method can be
     * passed parameters from the plan. Add all desired plan parameters as
     * method parameters, e.g. <tt>public void init({@literal @}Param("$speed")
     * Integer runningSpeed)</tt>.
     */
    public void init() {
        // Add your initialization code
    }

    /**
     * Method called during each tick of the logic the action is supposed to
     * run.
     *
     * The method can be passed parameters from the plan, e.g. <tt>public void
     * run({@literal @}Param("$stuckTime") Double stuckTimeSecs)</tt>.
     */
    public ActionResult run() {
        if( ctx.otherDefendersCount == 2){
            ctx.teamRole = TeamRoles.HEAD;
            ctx.SendNotThisRoleAnymore(TeamRoles.DEFENDER);
            ctx.SendNewRole(TeamRoles.HEAD);
        }
            
        else if(ctx.otherDefendersCount == 1){
             ctx.teamRole = TeamRoles.TAIL;
             ctx.SendNotThisRoleAnymore(TeamRoles.DEFENDER);
             ctx.SendNewRole(TeamRoles.TAIL);
        }
        
        return ActionResult.FINISHED;
    }

    /**
     * Method called once engine decides that another action should be run,
     * place you cleanup code here.
     *
     * The method can be passed parameters from the plan, e.g. <tt>public void
     * done({@literal @}Param("$notify") Boolean notifyTeam)</tt>.
     */
    public void done() {
        // Add your cleanup code here
    }
}
