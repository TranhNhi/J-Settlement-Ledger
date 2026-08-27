package com.example.J_Settlement.Ledger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Quan trọng để TimeBasedStrategy hoạt động
public class BlockchainConfig {}