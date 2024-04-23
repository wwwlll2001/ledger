package com.assignment.ledger.transaction.adapter.message;


/**
 * Message types definition for transaction message.
 *
 * <p>REQUEST_START: the ledger client want to start transactions in message mode, the type in the message should be
 *                REQUEST_START.
 *
 * <p>INIT: after ledger receive transaction requests no matter synchronous mode or asynchronous, ledger will save the
 *       requests in the db and broadcast the transaction started message, the message type in the message is INIT.
 *
 * <p>COMPLETED: when ledger capture the message in type INIT, leger will handle the transaction and send the COMPLETED
 *            message when finish the process.
 *            The ledger client should subscribe the message to get the transaction process result.
 *
 * <p>UPDATE: when ledger receive the transaction update request , will send the update request message in type UPDATE.
 *
 * <p>UPDATE_FAILED and UPDATE_SUCCESS: after ledger finish updating transaction, the update result will be sent in
 *                                      these two type.
 *                                      The ledger client should subscribe the message to get the update result.
 */
public enum TransactionMessageType {
    REQUEST_START, INIT, COMPLETED, UPDATE, UPDATE_FAILED, UPDATE_SUCCESS
}
