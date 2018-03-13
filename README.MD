# Datafari ML Ranking Module



## Prepare Datafari

Copy the ltr jar (located in Solr binary \solr-X.Y.Z\distsolr-ltr-X.Y.Z.jar) to a new lib folder : 
```
/opt/datafari/solr/solrcloud/FileShare/lib/ltr
```

### Copy LTR Lib

Add a new lib path to the new lib folder in solrconfig.xml of the Datafari FileShare core (located in /opt/datafari/solr/solrcloud/FileShare/conf):

```
<lib dir="${lib.path}lib/ltr"/>
```

### Load the ltr components in solr config

Add the following component to solrconfig.xml of the Datafari FileShare core :

```xml
<!-- Feature Values Cache : Cache used by the Learning To Rank (LTR) contrib module-->
<cache enable="true" name="QUERY_DOC_FV"
     class="solr.search.LRUCache"
     size="4096"
     initialSize="2048"
     autowarmCount="4096"
     regenerator="solr.search.NoOpRegenerator" />
<!--  LTR query parser : Query parser is used to rerank top docs with a provided model -->
<queryParser enable="true" name="ltr" class="org.apache.solr.ltr.search.LTRQParserPlugin"/>
<!--
      LTR Transformer will encode the document features in the response. For each document the transformer
      will add the features as an extra field in the response. The name of the field will be the name of the transformer enclosed between brackets (in this case [features]).
      In order to get the feature vector you will have to specify that you
      want the field (e.g., fl="*,[features])
-->
<transformer enable="true" name="features" class="org.apache.solr.ltr.response.transform.LTRFeatureLoggerTransformerFactory">
    <str name="fvCacheName">QUERY_DOC_FV</str>
</transformer>
```

### Reload config

Load the new configuration:

```
In /opt/datafari/bin/zkUtils
```

Run :

```
./uploadconfigzk.sh /opt/datafari 127.0.0.1:2181 FileShare
```

and then run :

```
./reloadCollections.sh FileShare
```

### Activate query reranking in search

Modify /opt/datafari/tomcat/webapps/Datafari/js/main.js file
to add the line (for example line 60):

Manager.store.addByValue("rq", '{!ltr model=DatafariModel reRankDocs=100}');

# Configuration

## Connection to Datafari
Connection to Datafari Cassandra and Datafari Solr can be modified in :
```
ml-ranking-module\src\main\resources\config.properties
```
## Features
Features to train model can be modified in :

```
ml-ranking-module\src\main\resources\featuresDatafari.json
```
## ML Ranking training process
The ML ranking training process is divided into different steps:
* Upload features to Solr
* Build training entries
* Train model
* Upload model to Solr

All these steps are detailled in main method :
```
com.datafari.ranking.Main.main
```
### Training entry score calculation

The way score for training entry is calculated can be modified in class : 
```
com.datafari.ranking.trainer.TrainingEntryScoreCalculator
```
  
More information on LTR : https://lucene.apache.org/solr/guide/learning-to-rank.html
