Elasticsearch Destination for syslog-ng
=======================================

Requirements
------------
 * gradle
 * SyslogNg.jar
 * JDK min 1.7 for building
 * JRE min 1.7 for usage
 
Building & Packaging
--------------------
Of course it is possible that you have to change the directory of the SyslogNg.jar
```
gradle -PsyslogDepsDir=/usr/lib/syslog-ng/3.6 build
```

Installing
----------
If you want to copy the generated elastic.jar into a specified directory you should call gradle install (directory can be set defining installDir property default is /usr/local/lib)
```
gradle -PinstallDir=/tmp/custom_install_dir install
```

There is a possibility to copy all the required jar files into a specified directory (installDir) using installDeps
```
gradle -PsyslogDepsDir=/usr/lib/syslog-ng/3.6 -PinstallDir=/tmp/custom_install_dir installDeps
```

Usage
-----
### Options
Name | Description | Required | Default value | example
-----| ----------- | -------- | ------------- | -------
server | the name of the ES server address or DNS | no | localhost | option("server", "127.0.0.1")
port | the port of the ES server (used only transport mode) | no | 9300 | options("port", "9300")
cluster | name of the cluster of the ES | ***yes*** | - | option("cluster", "syslog-ng")
index | name of index which stores the messages this can be a template | ***yes*** | - | option("index", "syslog-ng_${YEAR}.${MONTH}.${DAY}")
type | name of the type inside the index | ***yes*** | - | option("type", "test")
message_template | the message formatter template, the result of this template will be sent to the ES | no | $(format-json --scope rfc5424 --exclude DATE --key ISODATE) | option("message_template", "$(format-json --scope rfc5424 --exclude DATE --key ISODATE)")
custom_id | Using this option it is possible to set custom id for inserted records if this option is not set the ES will generate id, use this option very carefully, because if the id exists the message won't be stored in the elasticsearch | no | - | option("custom_id", "$RCPTID")
flush_limit | This destination sends log to ES using BulkInsert the number of messages in a bulk is set by this option | no | 1 | option("flush_limit", "100")
client_mode | This switch between *transport* and  *node* client mode, you can read more about this in the document of ElasticSearch | no | transport | option("client_mode", "node")
resource | Elesticsearch configuration file to load in case of node mode | no | - | options("resource", "/tmp/elasticsearch.yml")

### configuration example
#### Environment
 * In this example let's suppose, that the SyslogNg.jar is under the directory */usr/lib/syslog-ng/3.7*
 * the elasticsearch libraries is under the */usr/share/elasticsearch/lib/* directory
 * the built jar file is under the [project directory]/build/lib directory (elastic.jar)
 
#### real example

```
@version: 3.7

options {
  threaded(yes);
  use_rcptid(yes);
};

source s_tcp {
  tcp(port(5555));
};

destination d_es {
  java(
    class_path("/usr/local/elastic.jar:/usr/share/elasticsearch/lib/*.jar")
    class_name("org.syslog_ng.elasticsearch.ElasticSearchDestination")
    option("index", "syslog-ng_${YEAR}.${MONTH}.${DAY}")
    option("type", "test")
    option("cluster", "syslog-ng")
    option("flush_limit", "100")
  );
};

log {
  source(s_tcp);
  destination(d_es);
  flags(flow-control);
};

```
