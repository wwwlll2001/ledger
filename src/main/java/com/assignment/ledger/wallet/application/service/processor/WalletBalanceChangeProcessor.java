package com.assignment.ledger.wallet.application.service.processor;

import com.assignment.ledger.wallet.application.dto.BalanceChangeDto;
import com.assignment.ledger.wallet.application.service.WalletSynchronizeService;
import io.cloudevents.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.assignment.ledger.utils.JsonHelper.getObjectMapper;
import static com.assignment.ledger.wallet.domain.WalletMessageType.BALANCE_CHANGE;

/**
 * This processor is used to synchronize balance change data to elasticsearch.
 */
@Component
@Slf4j
public class WalletBalanceChangeProcessor implements WalletEventProcessor {

    private final WalletSynchronizeService walletSynchronizeService;

    public WalletBalanceChangeProcessor(WalletSynchronizeService walletSynchronizeService) {
        this.walletSynchronizeService = walletSynchronizeService;
    }

    @Override
    public boolean canProcess(CloudEvent event) {
        return BALANCE_CHANGE.toString().equals(event.getType());
    }

    @Override
    public void process(CloudEvent event) {
        String payload = new String((event.getData().toBytes()));
        try {
            BalanceChangeDto balanceChangeDto = getObjectMapper().readValue(payload, BalanceChangeDto.class);
            walletSynchronizeService.synchronizeBalanceChange(balanceChangeDto);
        } catch (Exception e) {
            // handle message error, should be monitored and take action if necessary
            log.error(e.getMessage(), e);
        }
    }
}
