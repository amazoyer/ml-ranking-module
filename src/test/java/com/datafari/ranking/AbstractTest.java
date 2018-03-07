package com.datafari.ranking;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.francelabs.ranking.dao.DatafariUsageDao;
import com.francelabs.ranking.dao.SolrClientProvider;
import com.francelabs.ranking.dao.SparkContextProvider;

@ContextConfiguration(classes = { DatafariUsageDao.class, SparkContextProvider.class, SolrClientProvider.class })
@RunWith(SpringRunner.class)
public class AbstractTest {

}
