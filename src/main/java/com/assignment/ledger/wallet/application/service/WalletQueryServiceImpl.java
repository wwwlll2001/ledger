package com.assignment.ledger.wallet.application.service;

import com.assignment.ledger.wallet.application.dto.WalletBalanceHistoryDocument;
import com.assignment.ledger.wallet.application.ports.in.WalletQueryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.assignment.ledger.common.Constant.WALLET_CHANGE_HISTORY_DOCUMENT_BALANCE;
import static com.assignment.ledger.common.Constant.WALLET_CHANGE_HISTORY_DOCUMENT_CHANGE_TIME;
import static com.assignment.ledger.common.Constant.WALLET_CHANGE_HISTORY_DOCUMENT_WALLET_ID;
import static com.assignment.ledger.common.Constant.WALLET_CHANGE_HISTORY_INDEX_NAME;

@Service
public class WalletQueryServiceImpl implements WalletQueryService {

    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    @Autowired
    public WalletQueryServiceImpl(ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }

    @Override
    public Mono<BigDecimal> getWalletBalanceAt(Long walletId, LocalDateTime queryTime) {
        Query query = buildQuery(walletId, queryTime);

        return reactiveElasticsearchOperations.search(query,
                                                      WalletBalanceHistoryDocument.class,
                                                      IndexCoordinates.of(WALLET_CHANGE_HISTORY_INDEX_NAME))
                                              .next()  // Retrieves the next element in this reactive stream
                                              .map(hit -> hit.getContent().getBalance());
    }

    @NotNull
    private static Query buildQuery(Long walletId, LocalDateTime queryTime) {
        Criteria criteria = new Criteria(WALLET_CHANGE_HISTORY_DOCUMENT_WALLET_ID).is(walletId)
                .and(new Criteria(WALLET_CHANGE_HISTORY_DOCUMENT_CHANGE_TIME).lessThanEqual(queryTime));

        Query query = new CriteriaQuery(criteria)
                .setPageable(PageRequest.of(0, 1))  // Limit to 1 result
                .addSort(Sort.by(Sort.Direction.DESC,
                                 WALLET_CHANGE_HISTORY_DOCUMENT_CHANGE_TIME)); // Sort by timestamp descending
        query.addFields(WALLET_CHANGE_HISTORY_DOCUMENT_BALANCE);
        return query;
    }
}
