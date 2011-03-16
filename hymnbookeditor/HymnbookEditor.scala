import java.io._;
import scala.xml._;

object HymnbookEditor {
  
  def main(args:Array[String]) {
    if(args.length != 2)
      printHelp
    else {
      val inFile = args(0);
      val outFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args(1)), "utf-8"));
      
      System.err.print("Parsing commands... ")
      //Read lines
      val in = new scala.io.BufferedSource(System.in);
      val lines = in.getLines;
      in.close
      //Parse commands
      val commands = 
      for(line <- lines) yield {
        val parts = noWhitespace(splitFirst(line, ":"))
        val range_parts = noWhitespace(parts(0).split("-"))
        val min = Integer.parseInt(range_parts(0))
        val max = Integer.parseInt(range_parts(1))
        val command_parts = noWhitespace(splitFirst(parts(1), "="))
        val element = command_parts(0)
        val value = if(command_parts(1)(0) == '<')
            XML.loadString(command_parts(1)).child
        else
            Text(command_parts(1))
        (min, max, element, value)
      }
      val ca = commands.toArray
      System.err.println("Found " + ca.length + " commands")
      System.err.println("Reading input file " + inFile) 
      //Read input file
      val src = XML.load(new org.xml.sax.InputSource(inFile))
      
      System.err.println("Performing transformations")
      //Transform XML tree
      val result = 
      src match {
        case <hymns>{ node @ _* }</hymns> => 
         <hymns>{ copyOrChange(node, ca) }</hymns>
      }
      System.err.println("Writing results to " + args(1))
      val output = new xml.PrettyPrinter(350, 2).format(result)
      //Output XML tree
      outFile.write(output)
      outFile.close
      System.err.println("Done!")
    }
  }
  
  /*
    Splits the string into two at the first detected separator.
  */
  def splitFirst(str:String, sep:String) : Array[String] = { 
    val parts = str.split(sep)
    Array(parts(0), parts.drop(1).mkString(sep))
  }

  def copyOrChange(iter : Seq[Node], commands : Array[(Int, Int, String, Seq[Node])]) =
  {
    for(node <- iter) yield {
      node match {
        case hymn @ <hymn>{ _* }</hymn> =>
          processHymn(hymn, commands) 
        case x => x
      }
    }
  }

  def processHymn(hymn : Node, commands : Array[(Int, Int, String, Seq[Node])]) : Node = {
    val number = Integer.parseInt((hymn \ "number")(0).text)
    for(command <- commands) {
      if(command._1 <= number && command._2 >= number)
        return applyCommand(hymn, command)
    } 
    hymn
  }

  def applyCommand(hymn : Node, command : (Int, Int, String, Seq[Node])) : Node = {
    val newElem = Elem(null, command._3, hymn.attributes, TopScope, command._4:_*)
    val numberElem = Elem(null, "number", hymn.attributes, TopScope, Text((hymn \ "number").text))
    val children = numberElem +: newElem +: hymn.child.filter {e => e.label != command._3 && e.label != "number"}
    Elem(null, "hymn", hymn.attributes, TopScope, children:_*)
  }

  def noWhitespace(strings:Array[String]) : Array[String] = {
    for(str <- strings if !str.isEmpty) yield {
      str.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
    }
  }


  def printHelp {
    print("Usage scala in.xml out.xml < commands.txt");
  }
}
