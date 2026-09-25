INSERT INTO ticket (id, title, description, status, priority, assignee, category, resolution_notes,
                    knowledge_state, knowledge_version, version, created_at, updated_at)
VALUES
('TKT-1001', 'Payment failed at checkout',
 'Customer reports card payment declined with error PAY-402 during checkout for order ORD-7781.',
 'RESOLVED', 'HIGH', 'alice', 'payments',
 'Root cause was an expired merchant API credential; credentials were rotated and checkout succeeded on retry.',
 'PENDING', 1, 0, NOW(), NOW()),
('TKT-1002', 'Shipment tracking not updating',
 'Tracking page stuck on label created for shipment SHP-2209 for 48 hours.',
 'IN_PROGRESS', 'MEDIUM', 'bob', 'shipping',
 NULL,
 'PENDING', 1, 0, NOW(), NOW()),
('TKT-1003', 'High priority payment retry loop',
 'Enterprise customer sees repeated payment failure notifications every 5 minutes.',
 'OPEN', 'HIGH', 'carol', 'payments',
 NULL,
 'PENDING', 1, 0, NOW(), NOW());

INSERT INTO ticket_comment (ticket_id, body, created_at) VALUES
('TKT-1001', 'Verified gateway logs show 402 responses from acquirer.', NOW()),
('TKT-1002', 'Carrier API latency spike suspected; monitoring carrier status page.', NOW());
