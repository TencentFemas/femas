Build Step:
1. add two local dependencies

cd ${project.basedir}/lib/org/jmxtrans/agent/jmxtrans-agent/1.2.10-SNAPSHOT;
mvn install:install-file -Dfile=jmxtrans-agent-1.2.10-SNAPSHOT.jar -DgroupId=org.jmxtrans.agent -DartifactId=jmxtrans-agent -Dversion=1.2.10-SNAPSHOT -Dpackaging=jar


cd ${project.basedir}/lib/one/profiler/async-profiler/0.1;
mvn install:install-file -Dfile=async-profiler-0.1.jar -DgroupId=one.profiler -DartifactId=async-profiler -Dversion=0.1 -Dpackaging=jar

Then
mvn clean package
