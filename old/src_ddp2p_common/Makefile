DIRS = scripts
all:
	-rm */*.class
	-rm */*/*.class
	javac -Xlint:unchecked -cp "jars/MultiSplit.jar:jars/javax.mail.jar:jars/sqlite4java.jar:jars/icepdf-viewer.jar:jars/icepdf-core.jar:jars/MetaphaseEditor-1.0.0.jar:./" MCMC/*.java handling_wb/*.java updates/*.java plugin_data/*.java streaming/*.java data/*.java table/*.java  util/*.java hds/*.java ASN1/*.java widgets/*/*.java util/*/*.java config/*.java wireless/*.java ciphersuits/*.java simulator/*.java tools/*.java WSupdate/*.java
	-xgettext -k_ */*.java
	-rm DD.jar
	echo "Main-Class: config.DD" > MANIFEST.MF

	sudo find ./ -not -name '*.java' -exec touch -t 200012121212.12 \{\} \;

	jar cmf MANIFEST.MF DD.jar */*.class */*/*.class *.properties p2pdd_resources/*.png p2pdd_resources/*.gif p2pdd_resources/*.ico p2pdd_resources/*.jpg p2pdd_resources/steag/* p2pdd_resources/census/*.jpg 

	jar cmf MANIFEST.MF DD_Android.jar */*.class  *.properties \
ASN1/*.java  ciphersuits/*.java  config/*.java  data/*.java  handling_wb/*.java  hds/*.java  MCMC/*.java  plugin_data/*.java  simulator/*.java  streaming/*.java  table/*.java   tools/*.java  updates/*.java  util/*.java  wireless/*.java  WSupdate/*.java

	#-./unit_test.sh util.updateJARfile DD.jar
	-cat dd_run_stub.sh DD.jar >dd_DD.sh
	-mkdir -p jars
	-cp DD.jar DD_Android.jar jars/
	-chmod +x dd_DD.sh
	-cd scripts; $(MAKE) $(MFLAGS)

translations:
	msgcat --properties-output messages.po >DebateDecide.properties
	msgcat --properties-output ./translations_path/ro_RO/LC_MESSAGES/DebateDecide.po >DebateDecide_ro.properties

force_look :
	true
