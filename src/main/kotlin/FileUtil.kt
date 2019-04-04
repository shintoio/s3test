
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import org.apache.commons.validator.routines.UrlValidator
import org.jets3t.service.S3ServiceException
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.URL
import java.nio.file.Paths

class FileUtil(accessKeyId: String = "", secretAccessKey: String = "") {

    private val schemes = arrayOf("http", "https", "s3", "s3a")
    private val urlValidator = UrlValidator(schemes)

   // private val credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey)

    fun downloadFile(remote: String, destination: String): Boolean {

        assert(isSupportedUrl(remote))
        val url = URL(remote)

        try {

            // https://s3-ap-southeast-2.amazonaws.com/amaterasu/BugBounty-TestUpload.txt
            val scheme = url.protocol //http
            if (scheme !in schemes) {
                throw IllegalArgumentException("${url.protocol} not supported")
            }

            val host = url.host // s3-ap-southeast-2.amazonaws.com
            val region: String = if (host == "s3.amazonaws.com") {
                "us-east-1" //N.Virginia
            } else {
                host.removePrefix("s3-").removeSuffix(".amazonaws.com")
            }

            val path = url.path.removePrefix("/") // /amaterasu/testfile.txt
            val split = path.split("/")
            val bucket = split[0]
            val key = split.subList(1, split.size).joinToString("/")

            val s3 = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.builder().build())
                .region(Region.of(region))
                .build()

            val request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()

            s3.getObject(request, ResponseTransformer.toFile(Paths.get(destination)))


            return true
        } catch (e: S3ServiceException) {
            System.err.println(e.message)
        } catch (e: FileNotFoundException) {
            System.err.println(e.message)
        } catch (e: IOException) {
            System.err.println(e.message)
        }
        return false
    }

    private fun isSupportedUrl(string: String): Boolean {
        return urlValidator.isValid(string)
    }

}