checkstyle:
		(./code/gradlew -p code/notificationBuilder checkstyle)

checkformat:
		(./code/gradlew -p code/notificationBuilder spotlessCheck)

format:
		(./code/gradlew -p code/notificationBuilder spotlessApply)

format-license:
		(./code/gradlew -p code licenseFormat)

javadoc:
		(./code/gradlew -p code/notificationBuilder javadocJar)

unit-test:
		(./code/gradlew -p code/notificationBuilder testPhoneDebugUnitTest)

unit-test-coverage:
		(./code/gradlew -p code/notificationBuilder createPhoneDebugUnitTestCoverageReport)

functional-test:
		(./code/gradlew -p code/notificationBuilder uninstallPhoneDebugAndroidTest)
		(./code/gradlew -p code/notificationBuilder connectedPhoneDebugAndroidTest)

functional-test-coverage:
		(./code/gradlew -p code/notificationBuilder createPhoneDebugAndroidTestCoverageReport)

assemble-phone:
		(./code/gradlew -p code/notificationBuilder  assemblePhone)

assemble-phone-release:
		(./code/gradlew -p code/notificationBuilder assemblePhoneRelease)

assemble-app:
		(./code/gradlew -p code/testapp  assemble)

notificationbuilder-publish-maven-local-jitpack: assemble-phone-release
		(./code/gradlew -p code/notificationBuilder publishReleasePublicationToMavenLocal -Pjitpack  -x signReleasePublication)

notificationbuilder-publish-snapshot: assemble-phone-release
		(./code/gradlew -p code/notificationBuilder publishReleasePublicationToSonatypeRepository)

notificationbuilder-publish: assemble-phone-release
		(./code/gradlew -p code/notificationBuilder  publishReleasePublicationToSonatypeRepository -Prelease)
