package lambda

import java.io.File

import common.FileUtils
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import domain.{SanghamMemberRequest, SnackRequest}
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.s3.AmazonS3Client
import com.lambdaworks.jacks.JacksMapper
import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

/**
  * Created by anil.mathew on 8/4/2017.
  */
class LambdaHandler {

    private val logger = LoggerFactory.getLogger(classOf[LambdaHandler])

    def processRequest(event: java.io.InputStream, out: java.io.OutputStream, context: Context) = {
        val json = Json.parse(scala.io.Source.fromInputStream(event).mkString)
        logger.info("Entering the LambdaHandler.processRequest method with json request => "+ json.toString())

        val trxState = (json \ "trxState").as[String]
        if (trxState.equalsIgnoreCase("ADD_MEMBER_STATE")) {
            addMember(json)
        }else if(trxState.equalsIgnoreCase("ADD_SNACK_STATE")){
            addSnackSignup(json)
        }else if(trxState.equalsIgnoreCase("GET_SNACK_MEMBER_STATE")){
            getSnackMembers(json, out)
        }
    }

    // This method will add the Member to the S3 file object
    def addMember(json: JsValue) = {

        logger.info("Getting the JSON and try to get memberlist from the request")
        var s3BucketName    = "sanghamca-bucket"

        val fileName      = "sanghamca_member_list.json"
        val s3client      = new AmazonS3Client()
        val format        = new java.text.SimpleDateFormat("yyMMddHHmmss")
        val directoryName = "sangham" + format.format(new java.util.Date())

        // If there is no file, getObject call will throw an error. If so, create the new file.
        try {
            logger.info("addMember => Calling getObject to get the file object")
            val s3Object       = s3client.getObject(s3BucketName, fileName)
            val memberRequest  = JacksMapper.readValue[SanghamMemberRequest](scala.io.Source.fromInputStream(s3Object.getObjectContent()).mkString)
            var memberList     = memberRequest.memberList
            logger.info("memberList:" + memberList);
            
            val newMemberRequest  = JacksMapper.readValue[SanghamMemberRequest](json.toString())
            val newMemberList    = newMemberRequest.memberList
            memberList =  memberList ::: newMemberList
            logger.info("memberList:" + memberList);

            val updatedMemberRequest = new SanghamMemberRequest("ADD_MEMBER_STATE", memberList)
            val newJson = JacksMapper.writeValueAsString[SanghamMemberRequest](updatedMemberRequest)
            logger.info("newJson:" + newJson);
            uploadMemberListToS3(directoryName, fileName, newJson, s3client, s3BucketName)
        }catch{
            case e: Throwable =>{
                logger.info("addMacAddress => Called getObject and no file object found:" + e)
                uploadMemberListToS3(directoryName, fileName, json.toString(), s3client, s3BucketName)
            }
        }
    }

    // This method will add the snack signup to the list
    def addSnackSignup(json: JsValue) = {

        logger.info("Getting the JSON and try to get snack memberlist from the request")
        var s3BucketName    = "ocg-snack-bucket"

        val fileName      = "snack_signup_list.json"
        val s3client      = new AmazonS3Client()
        val format        = new java.text.SimpleDateFormat("yyMMddHHmmss")
        val directoryName = "ocg" + format.format(new java.util.Date())

        // If there is no file, getObject call will throw an error. If so, create the new file.
        try {
            logger.info("addMember => Calling getObject to get the file object")
            val s3Object        = s3client.getObject(s3BucketName, fileName)
            val snackRequest    = JacksMapper.readValue[SnackRequest](scala.io.Source.fromInputStream(s3Object.getObjectContent()).mkString)
            var snackMemberList = snackRequest.snackMemberList
            logger.info("snackMemberList:" + snackMemberList);

            val newMemberRequest    = JacksMapper.readValue[SnackRequest](json.toString())
            val newSnackMemberList  = newMemberRequest.snackMemberList
            snackMemberList =  snackMemberList ::: newSnackMemberList
            logger.info("snackMemberList:" + snackMemberList);

            val updatedSnackMemberRequest = new SnackRequest("ADD_SNACK_STATE", snackMemberList)
            val newJson = JacksMapper.writeValueAsString[SnackRequest](updatedSnackMemberRequest)
            logger.info("newJson:" + newJson);
            uploadMemberListToS3(directoryName, fileName, newJson, s3client, s3BucketName)
        }catch{
            case e: Throwable =>{
                logger.info("addSnackSignup => Called getObject and no file object found:" + e)
                uploadMemberListToS3(directoryName, fileName, json.toString(), s3client, s3BucketName)
            }
        }
    }

    // This method will get the list of snack members
    def getSnackMembers(json: JsValue, out: java.io.OutputStream) = {
        logger.info("Entering getSnackMembers1");
        val s3BucketName    = "ocg-snack-bucket"
        val fileName        = "snack_signup_list.json"
        val s3client        = new AmazonS3Client()
        logger.info("Entering getSnackMembers2");
        val s3Object        = s3client.getObject(s3BucketName, fileName)
        val s3Data          = scala.io.Source.fromInputStream(s3Object.getObjectContent()).mkString
        logger.info("Entering getSnackMembers3 = >" + s3Data);
        val snackRequest    = JacksMapper.readValue[SnackRequest](s3Data)
        logger.info("Entering getSnackMembers4 => "+ snackRequest);
        val snackReq = new SnackRequest("GET_SNACK_MEMBER_STATE", snackRequest.snackMemberList)
        logger.info("Entering getSnackMembers5");
        val jsonData    = JacksMapper.writeValueAsString[SnackRequest](snackReq)
        logger.info("Entering getSnackMembers6 =>" + jsonData);
        out.write(jsonData.getBytes())
        out.close()
    }

    // This method will upload the member list to S3
    def uploadMemberListToS3(directoryName: String, fileName: String, json: String, s3client: AmazonS3Client, s3BucketName: String) = {

        FileUtils.mkdirs(List("/tmp", directoryName))
        FileUtils.mkdirs(List("/tmp/"+directoryName))

        Logger.info("Trying to write a file")
        val fileNameWithPath = "/tmp/" + directoryName + "/" + fileName
        FileUtils.writeToFile(fileNameWithPath, json);

        // create a PutObjectRequest passing the folder name suffixed by /
        val awsKey = fileName
        val putObjectRequest = new PutObjectRequest(s3BucketName, awsKey, new File(fileNameWithPath));
        putObjectRequest.setCannedAcl(CannedAccessControlList.PublicReadWrite);

        // send request to S3 to create folder
        Logger.info("uploadMemberListToS3=> Calling the putObject")
        s3client.putObject(putObjectRequest);
        Logger.info("uploadMemberListToS3=> Called the putObject")
    }
}
