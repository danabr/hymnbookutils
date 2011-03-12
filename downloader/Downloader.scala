import java.net

/**
  Deals explicitly with downloading files.
**/
object Downloader
{
  /**
    Downloads the contents of the given URL.
    The downloaded content is returned as a string.
  **/
  def downloadToString(url:String):String = 
  {
    val data = new StringBuilder
    
    //Get content
    val source = io.Source.fromURL(url, "latin1")
    source.getLines.foreach(data.append(_))
    
    return data.toString()
  }
}
