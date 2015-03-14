DIRS = scripts
all:
	-rm net/ddp2p/*/*.class
	-rm net/ddp2p/common/*/*.class
	-rm net/ddp2p/java/*/*.class
	-rm net/ddp2p/widgets/*/*.class
	-rm */*.class
	-rm util/tools/*.class
	-rm widgets/app/*.class
	javac -Xlint:unchecked -cp "jars/MultiSplit.jar:jars/javax.mail.jar:jars/sqlite4java.jar:jars/icepdf-viewer.jar:jars/icepdf-core.jar:jars/MetaphaseEditor-1.0.0.jar:./ "  net/ddp2p/ASN1/*.java  net/ddp2p/ciphersuits/*.java net/ddp2p/common/MCMC/*.java net/ddp2p/common/handling_wb/*.java net/ddp2p/common/updates/*.java net/ddp2p/common/plugin_data/*.java net/ddp2p/common/streaming/*.java net/ddp2p/common/data/*.java net/ddp2p/common/table/*.java net/ddp2p/common/util/*.java net/ddp2p/common/hds/*.java net/ddp2p/common/config/*.java net/ddp2p/common/wireless/*.java net/ddp2p/common/simulator/*.java net/ddp2p/common/WSupdate/*.java net/ddp2p/common/recommendationTesters/*.java net/ddp2p/common/examplePlugin/*.java  net/ddp2p/widgets/*/*.java  net/ddp2p/java/*/*.java tools/*.java util/tools/*.java widgets/app/*.java
	-xgettext -k_ tools/*.java util/tools/*.java net/ddp2p/*/*.java widgets/app/*.java net/ddp2p/widgets/*/*.java net/ddp2p/java/*/*.java net/ddp2p/common/*/*.java
	-rm DD.jar
	echo "Main-Class: net.ddp2p.widgets.app.MainFrame" > MANIFEST.MF

	sudo find ./ -not -name '*.java' -exec touch -t 200012121212.12 \{\} \;

	jar cmf MANIFEST.MF DD.jar  widgets/app/*.class util/tools/*.class tools/*.class net/ddp2p/*/*.class net/ddp2p/common/*/*.class */*.class net/ddp2p/widgets/*/*.class net/ddp2p/java/*/*.class *.properties p2pdd_resources/*.png p2pdd_resources/*.gif p2pdd_resources/*.ico p2pdd_resources/*.jpg p2pdd_resources/steag/* p2pdd_resources/census/*.jpg 

	jar cmf MANIFEST.MF DD_Android.jar net/ddp2p/ASN1/*.class net/ddp2p/ciphersuits/*.class net/ddp2p/common/*/*.class  *.properties \
net/ddp2p/ASN1/*.java  net/ddp2p/ciphersuits/*.java  net/ddp2p/common/config/*.java  net/ddp2p/common/data/*.java  net/ddp2p/common/handling_wb/*.java  net/ddp2p/common/hds/*.java net/ddp2p/common/MCMC/*.java  net/ddp2p/common/plugin_data/*.java  net/ddp2p/common/simulator/*.java  net/ddp2p/common/streaming/*.java  net/ddp2p/common/table/*.java net/ddp2p/common/updates/*.java  net/ddp2p/common/util/*.java net/ddp2p/common/wireless/*.java  net/ddp2p/common/WSupdate/*.java  net/ddp2p/common/recommendationTesters/*.java  net/ddp2p/common/examplePlugin/*.java

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