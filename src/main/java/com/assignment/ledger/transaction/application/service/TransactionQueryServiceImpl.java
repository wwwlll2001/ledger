package com.assignment.ledger.transaction.application.service;

import com.assignment.ledger.transaction.application.dto.TransactionListDto;
import com.assignment.ledger.transaction.application.dto.document.TransactionDocument;
import com.assignment.ledger.transaction.application.ports.in.TransactionQueryService;
import com.assignment.ledger.transaction.domain.TransactionStatus;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.assignment.ledger.common.Constant.TRANSACTION_DOCUMENT_FROM_WALLET_ID;
import static com.assignment.ledger.common.Constant.TRANSACTION_DOCUMENT_STATUS;
import static com.assignment.ledger.common.Constant.TRANSACTION_DOCUMENT_TO_WALLET_ID;

@Service
public class TransactionQueryServiceImpl implements TransactionQueryService {

    private final ReactiveElasticsearchOperations reactiveElasticsearchOperations;

    public TransactionQueryServiceImpl(ReactiveElasticsearchOperations reactiveElasticsearchOperations) {
        this.reactiveElasticsearchOperations = reactiveElasticsearchOperations;
    }

    @Override
    public Flux<TransactionListDto> retrieveTransactions(Long fromWalletId, Long toWalletId, TransactionStatus status) {
        CriteriaQuery query = buildQuery(fromWalletId, toWalletId, status);
        return reactiveElasticsearchOperations.search(query, TransactionDocument.class)
                .map(SearchHit::getContent)
                .map(TransactionListDto::from);
    }

    @NotNull
    private static CriteriaQuery buildQuery(Long fromWalletId, Long toWalletId, TransactionStatus status) {
        Criteria criteria = new Criteria();

        if (null != fromWalletId) {
            criteria = criteria.and(new Criteria(TRANSACTION_DOCUMENT_FROM_WALLET_ID).is(fromWalletId));
        }
        if (null != toWalletId) {
            criteria = criteria.and(new Criteria(TRANSACTION_DOCUMENT_TO_WALLET_ID).is(toWalletId));
        }
        if (null != status) {
            criteria = criteria.and(new Criteria(TRANSACTION_DOCUMENT_STATUS).is(status));
        }

        return new CriteriaQuery(criteria);
    }
}
