# SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
# cd $SCRIPT_DIR
# DATE=`date '+%Y-%m-%d_%H-%M-%S'`
# java -XX:+UseG1GC -Xms1024m -Xmx1024m -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -jar $SCRIPT_DIR/insight-1.0-SNAPSHOT.jar | tee $SCRIPT_DIR/out_$DATE.log
# java -XX:+UseG1GC -Xms1024m -Xmx1024m -jar $SCRIPT_DIR/insight-1.0-SNAPSHOT.jar
cd /Users/vitalykravchenko/projects/kotlin/insight/build/libs
# java -XX:+UseG1GC -Xms1024m -Xmx1024m -jar insight-1.0-SNAPSHOT.jar
java -jar insight-1.0-SNAPSHOT.jar