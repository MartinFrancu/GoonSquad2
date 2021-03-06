package ctfbot.action.ctf;


import ctfbot.CTFBotContext;
import cz.cuni.amis.pogamut.sposh.context.UT2004Context;
import cz.cuni.amis.pogamut.sposh.executor.ActionResult;
import cz.cuni.amis.pogamut.sposh.executor.Param;
import cz.cuni.amis.pogamut.sposh.executor.ParamsAction;
import cz.cuni.amis.pogamut.sposh.executor.PrimitiveInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.utils.collections.MyCollections;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Action PickDecentWeapons for Yaposh.
 *
 * @author student
 * @param <CONTEXT> Context class of the action. It's an shared object used by
 * all primitives. it is used as a shared memory and for interaction with the
 * environment.
 */
@PrimitiveInfo(name = "PickDecentWeapons", description = "Description of PickDecentWeapons")
public class PickDecentWeapons<CONTEXT extends CTFBotContext> extends ParamsAction<CONTEXT> {
    
    private Item targetItem;
    /**
     * Constructor of the action, used during automatic instantiation.
     */
    public PickDecentWeapons(CONTEXT ctx) {
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
        // Add your progress code
        ctx.getLog().warning("ACTION: PickDecentWeapons[Seeking for weapons]");
        
        if (ctx.getNavigation().isNavigatingToItem()) {
            return ActionResult.RUNNING;
        }

        List<Item> interesting = new ArrayList<Item>();
        
        
        // ADD QUADS
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.MINIGUN).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.SHOCK_RIFLE).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.FLAK_CANNON).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.LIGHTNING_GUN).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.ROCKET_LAUNCHER).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.BIO_RIFLE).values());
        interesting.addAll(ctx.getItems().getSpawnedItems(UT2004ItemType.U_DAMAGE_PACK).values());
        
        targetItem = MyCollections.getRandom(ctx.tabooItems.filter(interesting));
        
        if (targetItem == null) {
            ctx.getLog().warning("NO ITEM TO RUN FOR!");
            if ( ctx.getNavigation().isNavigating()) {
                 return ActionResult.RUNNING;   
            }
            
            ctx.getNavigation().navigate(ctx.getNavPoints().getRandomNavPoint());
        } else {
            
            ctx.getLog().log(Level.INFO, "RUNNING FOR: {0}", targetItem.getType().getName());
          
            ctx.getNavigation().navigate(targetItem);
        }
                
        return ActionResult.RUNNING_ONCE;
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
