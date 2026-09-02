package dansplugins.kdrtracker.commands;

import dansplugins.kdrtracker.data.PersistentData;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import preponderous.ponder.minecraft.bukkit.abs.AbstractPluginCommand;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Daniel McCoy Stephenson
 *
 * Regression coverage for every permission node that ponder's PermissionChecker
 * can consult being declared in plugin.yml. An undeclared node has no declared
 * default in Bukkit and so falls back to op-only, which would gate a command
 * more tightly than its documented sibling node does.
 */
class CommandPermissionRegistrationTest {

    /**
     * The commands handed to ponder's CommandService in KDRTracker#initializeCommandService.
     * DefaultCommand is deliberately absent: KDRTracker#onCommand constructs and executes it
     * directly, so its kdrt.default node is never consulted.
     */
    private List<AbstractPluginCommand> commandsRegisteredWithPonder() {
        return Arrays.asList(new HelpCommand(), new InfoCommand(new PersistentData()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> declaredPermissions() throws Exception {
        try (InputStream pluginYml = getClass().getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(pluginYml, "plugin.yml should be on the test classpath");
            Map<String, Object> descriptor = new Yaml().load(pluginYml);
            Map<String, Object> permissions = (Map<String, Object>) descriptor.get("permissions");
            assertNotNull(permissions, "plugin.yml should declare a permissions section");
            return permissions;
        }
    }

    @SuppressWarnings("unchecked")
    private Object declaredDefaultOf(Map<String, Object> permissions, String permission) {
        return ((Map<String, Object>) permissions.get(permission)).get("default");
    }

    @Test
    void everyPermissionPonderCanConsultIsDeclaredInPluginYml() throws Exception {
        Map<String, Object> permissions = declaredPermissions();
        for (AbstractPluginCommand command : commandsRegisteredWithPonder()) {
            for (String permission : command.getPermissions()) {
                assertTrue(permissions.containsKey(permission),
                        permission + " is passed to ponder's PermissionChecker but is not declared in plugin.yml");
            }
        }
    }

    @Test
    void kdrtViewIsGrantedByDefaultLikeItsKdrtInfoSibling() throws Exception {
        Map<String, Object> permissions = declaredPermissions();
        // PermissionChecker#checkPermission grants access when the sender holds any one of a
        // command's nodes, so the two nodes on InfoCommand have to agree on their default for
        // the alias to behave like the command it aliases.
        assertEquals(declaredDefaultOf(permissions, "kdrt.info"), declaredDefaultOf(permissions, "kdrt.view"));
    }

    @Test
    void kdrtDefaultIsNotDeclaredBecauseNothingConsultsIt() throws Exception {
        assertFalse(declaredPermissions().containsKey("kdrt.default"),
                "kdrt.default gates nothing, so declaring it would advertise a permission that has no effect");
    }
}
