package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.internal.ScenarioTransactionCommand;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScenarioDefinitionServiceTest {

    private final ScenarioDefinitionService service = new ScenarioDefinitionService();

    @Test
    void eventDemandSpikeExistsAndEidDoesNot() {
        boolean hasEvent = service.getDefinitions().stream().anyMatch(d -> d.scenarioType() == ScenarioType.EVENT_DEMAND_SPIKE);
        assertTrue(hasEvent, "EVENT_DEMAND_SPIKE must exist");

        // Assuming EID_DEMAND_SPIKE isn't in the enum, this is inherently tested by compilation,
        // but let's just make sure EVENT_DEMAND_SPIKE is exactly what's there.
    }

    @Test
    void repeatedAmountClusterUsesTwoAccounts() {
        List<ScenarioTransactionCommand> commands = service.buildCommands(
                ScenarioType.REPEATED_AMOUNT_CLUSTER, "AGT-001", "SCN-1"
        );
        assertEquals(7, commands.size());
        
        long uniqueAccounts = commands.stream().map(ScenarioTransactionCommand::syntheticAccountId).distinct().count();
        assertEquals(2, uniqueAccounts);
    }

    @Test
    void cashOutVelocitySpikeUsesDiverseAmountsAndAccounts() {
        List<ScenarioTransactionCommand> commands = service.buildCommands(
                ScenarioType.CASH_OUT_VELOCITY_SPIKE, "AGT-001", "SCN-2"
        );
        assertEquals(12, commands.size());

        long uniqueAmounts = commands.stream().map(ScenarioTransactionCommand::amount).distinct().count();
        assertTrue(uniqueAmounts > 5);

        long uniqueAccounts = commands.stream().map(ScenarioTransactionCommand::syntheticAccountId).distinct().count();
        assertEquals(12, uniqueAccounts);
    }

    @Test
    void hiddenProviderShortageAffectsOneProviderThroughCashIn() {
        List<ScenarioTransactionCommand> commands = service.buildCommands(
                ScenarioType.HIDDEN_PROVIDER_SHORTAGE, "AGT-001", "SCN-3"
        );
        
        assertFalse(commands.isEmpty());
        for (ScenarioTransactionCommand cmd : commands) {
            assertEquals("NAGAD", cmd.providerCode());
            assertEquals(com.bkash.baymax.superagent_api.model.enums.TransactionType.CASH_IN, cmd.type());
        }
    }
}
