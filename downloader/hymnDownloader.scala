import java.io._
import scala.collection.mutable.LinkedList

/**
  Application for downloading hymns
  from hem.bredband.net
  Hymns are downloaded to hymns.xml
**/
object HymnDownloader
{
  /**
    Argument 1:
      Hymnbook number (1937, 1819, etc)
    Argument 2 (3):
      n     : Hymn number n
      n,    : All hymns starting from n
      ,n    : All hymns from 1 to n
      a, b  : All hymns from a to b
  **/
  def main(args:Array[String]) {
    if(args.length <= 1) 
      printHelp
    else {
      val (hymnBook, startHymn, endHymn) = parseArguments(args)
      if(validHymnRange(startHymn, endHymn))
      {
        var outputFile = new PrintWriter(new OutputStreamWriter(new FileOutputStream("hymns.xml"), java.nio.charset.Charset.forName("UTF-8")))
        var baseURL = "http://hem.bredband.net/psalmer/SvPs" + 
              hymnBook + "/Nr"
        
        val pp = new xml.PrettyPrinter(350, 2) 
        var failedInARow = 0
        var failedHymns = new LinkedList[Int]()
        var num = startHymn
        while(num <= endHymn && failedInARow < 5) {
          try {
            print("Downloading hymn " + num + "\t")

            val html = Downloader.downloadToString(baseURL + num + ".html") 
            outputFile.println(pp.format(htmlToXML(html)))
            
            failedInARow = 0
            println("OK")
          } 
          catch { 
            case e => { //Hymn not found
              failedHymns = failedHymns :+ num
              println(e.getClass().getName())
              failedInARow += 1
            }
          }
          num += 1
        }
      
        //Finished
        outputFile.close
        
        if(failedInARow > 0)
          println("Multiple failed attempts in a row, halting.")
        else
          println("Finished")
        if(failedHymns.size > 0) {
          println("Failed to download the following hymns: " +
            failedHymns.mkString(", "))
        }
      } 
      else
        println("Invalid hymn number range: " + startHymn + " - " + endHymn)
    }
 
  }

  private def getAuthorsXML(str:String) = {
    var dotPattern = """\.*\s*$"""
    val emptyPattern  = """^\s*$"""
    var authorString = str.replaceAll("""\-\.""", "")
    val authors = authorString.split('\n')
    for(author <- authors if !author.matches(emptyPattern)) yield {
      <author> 
        <name>{ author.replaceFirst(dotPattern, "") }</name>
        <year></year>
      </author>
    }
  }

  private def getVersesXML(verses:LinkedList[String]) = {
    for((v, i) <- verses.zipWithIndex) yield {
      <verse number={ (i+1).toString }>{v.replaceAll("\n","")}</verse> 
    }
  }

  private def htmlToXML(html:String):xml.Elem = {
    val text = purify(html)
    val currentLine = text.lines
    val number = currentLine.next
    var title = ""
    
    //Parses verses and authors
    var lineNumberPattern = """^\**\d+\.*\s*"""
    var verses = new LinkedList[String]()
    var currentVerse = ""
    currentLine.foreach {(line:String) =>
      if(!line.matches("^\\s*$"))
      {
        if(title.isEmpty()) {
          val pure = line.replaceFirst(lineNumberPattern,"")
          title = pure.replaceFirst("\\,$", "") //Remove trailing ,
          currentVerse = pure + "\n"
        }
        else
          currentVerse = currentVerse + line.replaceFirst(lineNumberPattern,"") + "\n"
      }
      else if(!currentVerse.isEmpty()) //Beginning of new verse
      {
        verses = verses :+ currentVerse
        currentVerse = ""
      }
    }
    
    val authorsXML = getAuthorsXML(verses.last) 
    verses = verses.take(verses.size - 1)
    val versesXML = getVersesXML(verses)

    <hymn>
      <number>{ number}</number> 
      <title>{title}</title>
      <category></category>
      <copyright></copyright>
      <authors>{ authorsXML }</authors>
      <verses>{ versesXML }</verses>
      <melodies>
      </melodies> 
    </hymn>
  }
  
  private def parseArguments(args:Array[String]) = {
    if(args.length == 2) {
      if(args(1).endsWith(","))
        (args(0), args(1).take(args(1).length-1).toInt, Int.MaxValue)
      else if(args(1).startsWith(","))
        (args(0), 1, args(1).drop(1).toInt)
      else
        (args(0), args(1).toInt, args(1).toInt)
    }
    else 
      (args(0), args(1).take(args(1).length-1).toInt, args(2).toInt)
  }

  private def purify(html:String):String = {
    replaceEntities(
      html.replaceFirst(".*<[hH]2>", "<h2>").replaceFirst("</(body)|(BODY)>.*", "").
      replaceFirst("<[hH]2>\\s*(\\d+)\\.*\\s*</[hH]2>", "$1\n").
      replaceAll("<[aA].*</[aA]>", "").replaceAll("<h1>.*</h1>", "").
      replaceAll("<[bB][rR]>", "\n").replaceAll("<[pP]>", "\n\n").
      replaceAll("</[pP]>", "").replaceAll("Psalt.\\s*\\d+\\.*", "").
      replaceAll("\\W*[\\s\n]*$", "") //trailing whitespace
    )
  }

  private def printHelp {
    println("Usage: ")
    println("HymnDownloader 1937 n\t Download hymn n")
    println("HymnDownloader 1937 n,\t Download all hymns starting from number n")
    println("HymnDownloader 1937 ,n\t Download all hymns from 1 to n")
    println("HymnDownloader 1937 n, m\t Download all hymns from n to m")
  }

  private def replaceEntities(str:String):String = {
    str.
      replaceAll("&ouml;", "ö").replaceAll("&aring;", "å").
      replaceAll("&auml;", "ä").replaceAll("&Ouml;", "Ö").
      replaceAll("&Aring;", "Å").replaceAll("&Auml;", "Ä").
      replaceAll("&uuml;", "ü").replaceAll("&euro;", "ä").
      replaceAll("&eacute;", "è").replaceAll("&euml;", "ë").
      replaceAll("&aelig;", "æ").replaceAll("&AElig;", "Æ").
      replaceAll("&nbsp;*", "").
      replaceAll("&oslash;", "ø").replaceAll("&#339;", "\u0153") 
  }

  private def validHymnRange(start:Int, end:Int):Boolean = {
    start <= end && start > 0
  }
}
