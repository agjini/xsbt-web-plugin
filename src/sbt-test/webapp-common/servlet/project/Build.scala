import sbt._
import com.earldouglas.xsbtwebplugin._
import WebPlugin._
import PluginKeys._
import Keys._

object MyBuild extends Build {
  override def projects = Seq(root)

  lazy val root = Project("root", file("."), settings = Defaults.defaultSettings ++ webSettings ++ rootSettings)

  def jettyPort = 7123

  def Conf = config("container")

  lazy val rootSettings = Seq(
    port in Conf := jettyPort,
    scanInterval in Compile := 60,
    libraryDependencies += "javax.servlet" % "servlet-api" % "2.5" % "provided",
    getPage := getPageTask,
    checkPage <<= checkPageTask,                  
    libraryDependencies ++= Seq(                  
      "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
      "org.mortbay.jetty" % "jsp-2.0" % "6.1.22" % "container"
    )
  )

  def indexURL = new java.net.URL("http://localhost:" + jettyPort)
  def indexFile = new java.io.File("index.html")

  lazy val getPage = TaskKey[Unit]("get-page")
  
  def getPageTask {
    indexURL #> indexFile !
  }

  lazy val checkPage = InputKey[Unit]("check-page")
  
  def checkPageTask = InputTask(_ => complete.Parsers.spaceDelimited("<arg>")) { result =>
    (getPage, result) map {
      (gp, args) =>
      checkHelloWorld(args.mkString(" ")) foreach error
    }        
  }

  private def checkHelloWorld(checkString: String) =
  {
    val value = IO.read(indexFile)
    if(value.contains(checkString)) None else Some("index.html did not contain '" + checkString + "' :\n" +value)
  }
}
