package com.jaffer.makeitweb.server;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;
import org.webswing.Constants;

public class JmsService {

    public static void startService() throws Exception {
        // BrokerService broker= BrokerFactory.createBroker("xbean:mq.xml");

        BrokerService broker = new BrokerService();
        broker.setUseJmx(false);
        broker.setPersistent(false);

        PolicyMap policyMap = new PolicyMap();
        PolicyEntry defaultEntry = new PolicyEntry();
        ConstantPendingMessageLimitStrategy pendingMessageLimitStrategy = new ConstantPendingMessageLimitStrategy();
        pendingMessageLimitStrategy.setLimit(10);
        defaultEntry.setPendingMessageLimitStrategy(pendingMessageLimitStrategy);
        policyMap.setDefaultEntry(defaultEntry);
        broker.setDestinationPolicy(policyMap);

        SystemUsage memoryManager = new SystemUsage();
        MemoryUsage memoryLimit = new MemoryUsage();
        memoryLimit.setLimit(20000000);

        memoryManager.setMemoryUsage(memoryLimit);
        broker.setSystemUsage(memoryManager);
        // configure the broker
        broker.addConnector(Constants.JMS_URL);

        broker.start();
    }
}
