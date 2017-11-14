package common

import java.io.{File, PrintWriter, FileWriter}

/**
  * Created by anil.mathew on 8/25/2016.
  */
object FileUtils {

    // Helper method for reading/writing to database, files, etc
    def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B = try { f(param) } finally { param.close() }

    // Method which write the data to a file
    def writeToFile(fileName:String, data:String) =
        using (new FileWriter(fileName)) {
            fileWriter => fileWriter.write(data)
        }

    // Method which append the data to an existing file
    def appendToFile(fileName:String, textData:String) =
        using (new FileWriter(fileName, true)){
            fileWriter => using (new PrintWriter(fileWriter)) {
                printWriter => printWriter.println(textData)
            }
        }

    def mkdirs(path: List[String]) = // return true if path was created
        path.tail.foldLeft(new File(path.head)){(a,b) => a.mkdir; new File(a,b)}.mkdir


}