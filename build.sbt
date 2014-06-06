lazy val `redikka-core` = CoreBuild.`redikka-core`

lazy val `redikka-tcp` = TcpBuild.`redikka-tcp` dependsOn `redikka-core`

lazy val `redikka-tests` = TestsBuild.`redikka-tests`
