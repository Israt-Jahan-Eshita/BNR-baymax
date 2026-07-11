package com.bkash.baymax.superagent_api.service;

import com.bkash.baymax.superagent_api.dto.internal.ScenarioTransactionCommand;
import com.bkash.baymax.superagent_api.dto.response.ScenarioDefinitionResponse;
import com.bkash.baymax.superagent_api.model.enums.ScenarioType;
import com.bkash.baymax.superagent_api.model.enums.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScenarioDefinitionService {

    public List<ScenarioDefinitionResponse> getDefinitions() {
        return List.of(
                new ScenarioDefinitionResponse(
                        ScenarioType.HIDDEN_PROVIDER_SHORTAGE,
                        "Hidden Provider Shortage",
                        "Decreases Nagad e-money significantly by multiple CASH_IN transactions, maintaining valid positive balances, to show unified visibility of separate funds.",
                        "No anomaly alert expected, but physical cash will increase and Nagad balance will decrease materially."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.EVENT_DEMAND_SPIKE,
                        "Event Demand Spike",
                        "Synthetic event-linked customer demand increase with diverse accounts and transaction amounts.",
                        "Should not produce any HIGH/CRITICAL alert for contextually normal activity."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.REPEATED_AMOUNT_CLUSTER,
                        "Repeated Amount Cluster",
                        "Injects 7 CASH_OUT transactions of nearly identical amounts across two synthetic accounts.",
                        "Expected to trigger REPEATED_AMOUNT_CLUSTER alert."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.PROVIDER_FEED_DELAY,
                        "Provider Feed Delay",
                        "Simulates a 30-minute delay in the provider feed data sync for Bkash.",
                        "No transaction injected. Provider health status changes to DELAYED."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.CONFLICTING_BALANCE_DATA,
                        "Conflicting Balance Data",
                        "Simulates conflicting balance snapshot data for Rocket.",
                        "Provider health status changes to CONFLICTING."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.CASH_OUT_VELOCITY_SPIKE,
                        "Cash Out Velocity Spike",
                        "Injects 12 varied CASH_OUT transactions rapidly from many distinct accounts.",
                        "Expected to trigger CASH_OUT_VELOCITY_SPIKE alert."
                ),
                new ScenarioDefinitionResponse(
                        ScenarioType.NORMAL,
                        "Normal Activity",
                        "Ordinary synthetic activity containing 3-4 mixed transactions.",
                        "Expected to remain alert-free."
                )
        );
    }

    public List<ScenarioTransactionCommand> buildCommands(
            ScenarioType type,
            String agentCode,
            String scenarioRunId
    ) {
        List<ScenarioTransactionCommand> commands = new ArrayList<>();

        switch (type) {
            case REPEATED_AMOUNT_CLUSTER -> {
                String providerCode = "BKASH";
                int[] amounts = {1950, 1975, 1980, 1990, 2000, 2010, 2025};
                for (int i = 0; i < amounts.length; i++) {
                    String account = (i % 2 == 0) ? "SIM-CLUSTER-001" : "SIM-CLUSTER-002";
                    commands.add(new ScenarioTransactionCommand(
                            agentCode, providerCode, TransactionType.CASH_OUT,
                            BigDecimal.valueOf(amounts[i]), account, scenarioRunId
                    ));
                }
            }
            case CASH_OUT_VELOCITY_SPIKE -> {
                String providerCode = "NAGAD";
                int[] amounts = {2100, 2600, 3150, 2250, 3700, 2850, 4200, 2350, 3400, 2950, 3900, 2500};
                for (int i = 0; i < amounts.length; i++) {
                    commands.add(new ScenarioTransactionCommand(
                            agentCode, providerCode, TransactionType.CASH_OUT,
                            BigDecimal.valueOf(amounts[i]), "SIM-VELOCITY-" + i, scenarioRunId
                    ));
                }
            }
            case EVENT_DEMAND_SPIKE -> {
                String providerCode = "BKASH";
                int[] amounts = {500, 1200, 800, 3100, 1500, 750, 2100};
                for (int i = 0; i < amounts.length; i++) {
                    commands.add(new ScenarioTransactionCommand(
                            agentCode, providerCode, TransactionType.CASH_OUT,
                            BigDecimal.valueOf(amounts[i]), "SIM-EVENT-" + i, scenarioRunId
                    ));
                }
            }
            case NORMAL -> {
                String providerCode = "ROCKET";
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_IN, BigDecimal.valueOf(1000), "SIM-NORM-001", scenarioRunId));
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_OUT, BigDecimal.valueOf(1500), "SIM-NORM-002", scenarioRunId));
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_IN, BigDecimal.valueOf(500), "SIM-NORM-003", scenarioRunId));
            }
            case HIDDEN_PROVIDER_SHORTAGE -> {
                String providerCode = "NAGAD";
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_IN, BigDecimal.valueOf(5000), "SIM-SHORTAGE-001", scenarioRunId));
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_IN, BigDecimal.valueOf(6000), "SIM-SHORTAGE-002", scenarioRunId));
                commands.add(new ScenarioTransactionCommand(agentCode, providerCode, TransactionType.CASH_IN, BigDecimal.valueOf(7000), "SIM-SHORTAGE-003", scenarioRunId));
            }
            default -> {}
        }

        return commands;
    }
}
