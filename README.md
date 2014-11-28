BFSCrawler
==========

com.hrant.crawler.BFSCrawler
  Holds main BFS logic.

Public Methods:
  
  void bfsLogic(LinkedList<String> queue)
    Main BFS logic. Gets initial list of urls, runs BFS search on it.
    
  static List<String> readLinksInTXT(Path inputPath)
    Reads initial urls from text file.

Private Methods

  Set<String> getAllLinksInPage(String pageUrl) throws IOException
    Extracts urls from given page. Ensures url uniqueness.
    
com.hrant.utils.Constants
  Holds constant values.
  
com.hrant.dao.OlxDAO
  Singleton.
  Holds database connection.
  
Public Methods:
  void addMessage(UrlEntry urlEntry)
    Saves entity in database.
    
com.hrant.model.UrlEntry
  Url entry model.
  
com.hrant.JPAUtil
  JPA implementation
