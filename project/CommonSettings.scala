import Dependencies.V
import com.awwsmm.sbt.DependencyUpdaterPlugin
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin
import org.apache.pekko.grpc.sbt.PekkoGrpcPlugin.autoImport.{
  PekkoGrpc,
  pekkoGrpcCodeGeneratorSettings,
  pekkoGrpcGeneratedSources
}
import org.scalafmt.sbt.ScalafmtPlugin
import sbt.*
import sbt.Keys.*
import sbt.nio.Keys.{ReloadOnSourceChanges, onChangedBuildSource}
import sbt.{Compile, Global, Project}
import sbtbuildinfo.BuildInfoPlugin
import sbtbuildinfo.BuildInfoPlugin.autoImport.{BuildInfoKey, buildInfoKeys, buildInfoPackage}
import sbtprotoc.ProtocPlugin.autoImport.PB

object CommonSettings {

  def configureBuildInfo(project: Project) = project
    .enablePlugins(BuildInfoPlugin)
    .settings(
      buildInfoPackage := s"${organization.value}.${normalizedName.value.replace('-', '_')}",
      buildInfoKeys := Seq[BuildInfoKey](
        name,
        normalizedName,
        description,
        homepage,
        startYear,
        organization,
        organizationName,
        version,
        scalaVersion,
        sbtVersion,
        Compile / allDependencies
      )
    )

  def commonSettings(project: Project): Project =
    project
      .settings(
        organization := "io.github.sfali23",
        version := "0.1.0-SNAPSHOT",
        scalaVersion := V.Scala2_13,
        Global / onChangedBuildSource := ReloadOnSourceChanges,
        resolvers ++= Seq(
          "Sonatype OSS" at "https://s01.oss.sonatype.org/content/groups/public/"
        )
      )
      .configure(configureBuildInfo)
      .enablePlugins(ScalafmtPlugin, DependencyUpdaterPlugin)

  def pekkoGrpcSettings(apiProject: Project, generatedSource: Seq[PekkoGrpc.GeneratedSource])(project: Project): Project =
    project
      .enablePlugins(PekkoGrpcPlugin)
      .settings(
        (Compile / PB.protoSources) += (apiProject / baseDirectory).value / "src" / "main" / "protobuf",
        pekkoGrpcGeneratedSources := generatedSource,
        pekkoGrpcCodeGeneratorSettings := Seq("grpc", "single_line_to_proto_string"),
        scalacOptions ++= Seq(
          "-Wconf:src=pekko-grpc/.*:silent" // Ignore warnings in classes generated by pekko-grpc
        )
      )
}
