INSERT INTO ledger_entity
(id, name)
VALUES(1, 'entity1');
INSERT INTO account
(id, ledger_entity_id, name, status, created_at, updated_at)
VALUES(1, 1, 'account1', 'OPEN', NULL, NULL);
INSERT INTO wallet
(id, account_id, name, balance, asset_type, version, created_at, updated_at)
VALUES(1, 1, 'FROM', 10000.0000, 'FLAT_CURRENCY', 29, NULL, NULL);
INSERT INTO wallet
(id, account_id, name, balance, asset_type, version, created_at, updated_at)
VALUES(2, 1, 'TO', 20000.0000, 'FLAT_CURRENCY', 29, NULL, NULL);
