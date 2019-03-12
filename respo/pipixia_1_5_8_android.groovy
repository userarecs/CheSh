package com.certus.bairen
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.ContentType.URLENC
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovyx.net.http.HTTPBuilder
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date
import java.text.DateFormat
import java.text.SimpleDateFormat

import org.apache.http.params.CoreConnectionPNames

import com.certus.ivam.groovy.AppDataStore
import com.certusnet.datacollection.groovy.video.VideoMetadata
class pipixia_1_5_8_android {
	static final def APP_NAME = '皮皮虾'
	static final def APP_TYPE = '2' //app 类型 android 2
	static final def SLEEP_TIME = 1000 * 60
	static final def TIME_OUT = 1000 * 15
	static final def MAX_LENGTH = 20
	static final def APP_PACKAGE_NAME = 'com.sup.android.superb.bairen' 
	static def arrayList = new ArrayList<VideoMetadata>()
	static SimpleDateFormat sdf_convert = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
	
	static def CHAR_SET = ('0' .. '9').toArray() + ('A' .. 'Z').toArray()
	static def JDBC_URL = 'jdbc:oracle:thin:@(description=(address_list= (address=(host=172.16.24.75)(protocol=tcp)(port=1521))(address=(host=172.16.24.76)(protocol=tcp) (port=1521)) (load_balance=yes)(failover=on))(connect_data=(service_name= orcl)))'
	static final boolean onLine = false;//TODO  是否为线上环境  true 线上环境； false 测试环境
	static final def SimpleDateFormat sdf_convert2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")//TODO ok 时间格式为 例如: 2018-06-25 00:00:00  date到字符串
	
	static final def SHELL_NAME = 'pipixia_1_5_8_android'
	static def INSERT_COUNT = 0;
	static boolean flag = true;
	static def lastHour = 0;
	static def nowHour = 0;
	
	static main(args) {
	}

	public Object run(Object obj) {
		for(i in 0..<300){
			getVideoList()
		}
//		action()
		return 'success'
	}
	
	static void action(){
		while(flag){
			getVideoList()
		}
		rest()
	}
	
	static void rest(){
		lastHour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
		while(!flag){
			nowHour= Calendar.getInstance().get(Calendar.HOUR_OF_DAY)//当前时间--时--24小时制
			println ">"+SHELL_NAME+">>nowHour："+nowHour
			if(nowHour==lastHour+1){
				flag = true
				INSERT_COUNT = 0
			}
			Thread.sleep(10000);
		}
		action()
	}
	
	static void getVideoList(){
		def http = new HTTPBuilder()
		try {
			def url = 'http://lf.snssdk.com/bds/feed/stream/?api_version=2&cursor=1550824403749&list_type=4&feed_count=5&direction=1&iid=64220827977&device_id=56211443546&ac=wifi&channel=oppo&aid=1319&app_name=super&version_code=158&version_name=1.5.8&device_platform=android&ssmix=a&device_type=ONEPLUS+A5000&device_brand=OnePlus&language=zh&os_api=28&os_version=9&uuid=56211443546&openudid=8df34530e67628eb&manifest_version_code=158&resolution=1080*1806&dpi=380&update_version_code=1583&_rticket=1550824406368&ts=1550824406&as=a2c54bd6161dac23ff6611&cp=b4d1c8576aff683fe2OaWe&mas=003c36d337dbaf316ef200c4aa9b920db6b3b353535959f373f9b9' 
			println SHELL_NAME+'>>>url:'+url
			http.setUri(url)
			//请求超时
			http.client.params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT)
			//读取超时
			http.client.params.setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT)
			
			http.request(GET, JSON) { req ->
				headers.'User-Agent' = 'com.sup.android.superb/158 (Linux; U; Android 9; zh_CN; ONEPLUS A5000; Build/PKQ1.180716.001; Cronet/58.0.2991.0)';
				headers.'X-SS-REQ-TICKET'='1550824406369';
				headers.'X-Gorgon'='030068360000d3dd5efed6aa86a8e46d065918f136da0ceda6ef'
				headers.'X-Pods'='47d94bfb307c707d513517fcafffc832b5feb96b'
				headers.'X-SS-TC'='0'
				headers.'X-SS-RS'='1'
				//请求成功
				response.success = { resp, jsons ->
					assert resp.statusLine.statusCode == 200
//					println '>>>jsons:::'+jsons
					if (jsons) {
						def data1 = jsons.get('data')
						if (data1) {
							def dataList = data1.get('data')
							if(dataList){
								dataList.each {
									def data = it
									if(data){
										def dataItem = data.get('item')
										if(dataItem){
											def id = dataItem.get('item_id_str')
											def videoMetadata = new VideoMetadata()
											videoMetadata.setId(String.valueOf(id))
											videoMetadata.setType("视频")
											videoMetadata.setUrlType(1)
											def share = dataItem.get('share')
											if(share){
												def share_url = share.get('share_url')
												def title = share.get('title')
												def desc = share.get('content')
												videoMetadata.setName(title)
												videoMetadata.setDesc(desc)
												videoMetadata.setShareUrl(share_url)
											}
											def stats = dataItem.get('stats')
											if(stats){
												def comment_count = stats.get('comment_count')
												def play_count = stats.get('play_count')
												def like_count = stats.get('like_count')
												def share_count = stats.get('share_count')
												videoMetadata.setCommentCount(comment_count)
												videoMetadata.setPlayCount(play_count)
												videoMetadata.setLikeCount(like_count)
												videoMetadata.setShareCount(share_count)
											}
											def author = dataItem.get('author')
											if(author){
												def uploadUserImgList = author.get('avatar')
												if(uploadUserImgList){
													def url_list = uploadUserImgList.get('url_list')
													if(url_list){
														def uploadUserImgs = url_list[0]
														if(uploadUserImgs){
															def uploadUserImg =  uploadUserImgs.get('url')
															videoMetadata.setUploadUserImg(uploadUserImg)
														}
													}
													
												}
												def uploadUserName = author.get('name')
												def uploadUserId = author.get('id_str')
												videoMetadata.setUploadUserId(uploadUserId)
												videoMetadata.setUploadUserName(uploadUserName)
											}
											def video = dataItem.get('video')
											if(video){
												def cover = video.get('cover_image')
												if(cover){
													def url_list = cover.get('url_list')
													if(url_list){
														def posterUrls = url_list[0]
														if(posterUrls){
															def posterUrl = posterUrls.get('url')
															videoMetadata.setPosterUrl(posterUrl)
														}
													}
												}
												def programUrlList = video.get('video_download')
												if(programUrlList){
													//废弃 该链接具有时效性
//													def url_list = programUrlList.get('url_list')
//													if(url_list){
//														def programUrls = url_list[0]
//														if(programUrls){
//															def programUrl = programUrls.get('url')
//															videoMetadata.setUrl(programUrl)
//														}
//													}
													def uri = programUrlList.get('uri')
													def programUrl = "http://api.huoshan.com/hotsoon/item/video/_playback/?video_id="+uri+"&line=0&app_id=13"
													videoMetadata.setUrl(programUrl)
												}
												def duration = video.get('duration')
												if(duration){
													duration =  duration*1000 as long
													videoMetadata.setVideoDuration(duration.toString())
													
												}
											}
											def create_time = dataItem.get('create_time')
											if(create_time){
												Long time= new Long(create_time+'000');
												String d = sdf_convert.format(time);
												Date date =sdf_convert.parse(d);
												videoMetadata.setReleaseTime(date)
											}
											saveData(videoMetadata)
										}
									}
								}
							}
						} else {
							println SHELL_NAME+'data---' + url + '---' + data1
						}
					} else {
						println SHELL_NAME+'jsons---' + url + '---' + jsons
					}
				}
				//其他错误，不实现则采用缺省的：抛出异常。
				http.handler.failure = { resp ->
					println SHELL_NAME+'Unexpected failure: ${resp.statusLine}'
				}
			}
		} catch (SocketException e) {
			println SHELL_NAME+'getVideoList---' + e.toString()
			Thread.sleep(SLEEP_TIME)
		} catch (Exception e) {
			println SHELL_NAME+'getVideoList---' + e.toString()
		} finally {
			if (null != http) {
				http.shutdown()
			}
		}
	}
	
	static void saveOrUpdate(ArrayList<VideoMetadata> arrayList) {

		Sql sql = getSqlInstance()
		String programName =""
		String programUrl = ""
		String programDesc = ""
		String posterUrl = ""
		String shareUrl = ""
		Integer commentCount = 0
		Integer shareCount = 0
		Integer likeCount = 0
		Integer playCount = 0
		String videoId = ""
		String videoDuration = ""
		String uploadUserId = ""
		String uploadUserName = ""
		String uploadUserImg = ""
		Date uploadTime =  new Date()
		for (VideoMetadata vv : arrayList) {
			try {
				if(vv.getName()&&vv.getUrl()){
					String videoKey = getMD5String(vv.getId(),vv.getName(),APP_NAME)//入库不区分Android和IOS 只区分媒体平台(app)
					def selectSql = "SELECT count(a.id) FROM IVMA_GD_PIPIXIA_VIDEO a WHERE a.video_key = '"+videoKey+"'"
					println '>>>执行查询语句selectSql:'+selectSql
					sql.eachRow(selectSql) { row ->
						//>>>row:[LAST_MODIFY_TIME:2018-06-07 13:50:52.0]
						//>>>time:2018-06-07 13:50:52.0
						println '>>>row:'+row
						def time = row['COUNT(A.ID)']
						println '>>>time:'+time
						if(time){
							println '>>更新'
							commentCount =  vv.getCommentCount()?vv.getCommentCount():0
							likeCount =  vv.getLikeCount()?vv.getLikeCount():0
							shareCount =  vv.getShareCount()?vv.getShareCount():0
							playCount =  vv.getPlayCount()?vv.getPlayCount():0
							programUrl = vv.getUrl()
							shareUrl = vv.getShareUrl()
							//更新
							sql.executeUpdate("update IVMA_GD_PIPIXIA_VIDEO set  last_modify_time=sysdate,comment_count=${commentCount},share_count=${shareCount},like_count=${likeCount},play_count=${playCount},program_url=${programUrl},share_url=${shareUrl} where video_key=${videoKey}");
						}else{
							println '>>新增'
							programName = vv.getName()
							programUrl =  vv.getUrl()
							programDesc = vv.getDesc()
							if(programDesc.length()>1000){
								programDesc = programDesc.substring(0,1000)
							}
							if(!programDesc){
								programDesc = ''
							}
							posterUrl =  vv.getPosterUrl()
							shareUrl= vv.getShareUrl()
//							commentCount =  Integer.parseInt(vv.getCommentCount()==null?'0':vv.getCommentCount());
//							likeCount =  Integer.parseInt(vv.getLikeCount()==null?'0':vv.getLikeCount());
//							shareCount =  Integer.parseInt(vv.getShareCount()==null?'0':vv.getShareCount());
							commentCount =  vv.getCommentCount()?vv.getCommentCount():0
							likeCount =  vv.getLikeCount()?vv.getLikeCount():0
							shareCount =  vv.getShareCount()?vv.getShareCount():0
							playCount =  vv.getPlayCount()?vv.getPlayCount():0
							videoId = vv.getId()
							videoDuration = vv.getVideoDuration()
							uploadUserId = vv.getUploadUserId()
							uploadUserName = vv.getUploadUserName()
							uploadUserImg = vv.getUploadUserImg()
							uploadTime = vv.getReleaseTime()
							String uploadTimeStr = ''
							if(uploadTime){
								uploadTimeStr = sdf_convert2.format(uploadTime)
							}else{
								//如果没有采集到上传时间 就用第一次采集时间作为上传时间
								uploadTimeStr = sdf_convert.format(new Date())
							}
							sql.execute("INSERT INTO IVMA_GD_PIPIXIA_VIDEO (id,program_title,  program_url,   program_desc,  poster_url, share_url, comment_count, share_count, like_count, play_count, video_id, video_duration, upload_user_id, upload_user_name, upload_user_img, upload_time, insert_time, last_modify_time, video_key) VALUES ( IVMA_GD_PIPIXIA_VIDEO\$SEQ.nextval,${programName},${programUrl},${programDesc},${posterUrl},${shareUrl},${commentCount},${shareCount},${likeCount},${playCount},${videoId},${videoDuration},${uploadUserId},${uploadUserName},${uploadUserImg},to_date(${uploadTimeStr}, 'yyyy-MM-dd HH24:mi:ss'),sysdate,sysdate,${videoKey})");
							INSERT_COUNT++
							println ">"+SHELL_NAME+">>INSERT_COUNT："+INSERT_COUNT
						}
//						Thread.sleep(TIME_OUT)
					}
				}
			} catch (Exception e) {
				println 'saveData---' + e.toString()
			}
			arrayList = new ArrayList<VideoMetadata>()
		}
	}
	static  getMD5String(String programId,String programName,String appName){
		def key = programId+programName+appName
		MessageDigest.getInstance("MD5").digest(key.getBytes()).inject(""){ result, ele ->
			result << CHAR_SET[((ele >>> 4) & 0xf)] << CHAR_SET[(ele & 0xf)]
		}
	}
	static Sql getSqlInstance() {
		Sql sql = null;
		if(onLine){
			//线上环境
			sql = Sql.newInstance(
					JDBC_URL,
					'ivma',
					'ivma',
					'oracle.jdbc.driver.OracleDriver')
		}else{
			//测试环境
			sql = Sql.newInstance(
					'jdbc:oracle:thin:@(DESCRIPTION = (ADDRESS = (PROTOCOL = TCP)(HOST = 172.16.39.212)(PORT = 1521)) (CONNECT_DATA =(SERVICE_NAME = RAC)))',
					'ivma',
					'ivma',
					'oracle.jdbc.driver.OracleDriver')
		}
		return sql
	}

	static void saveData(VideoMetadata video) {
		if(video.getName()&&video.getUrl()){
			arrayList.add(video)
			println arrayList.size
		}else{
			println ">"+SHELL_NAME+">>残缺数据：title："+video.getName()
			println ">"+SHELL_NAME+">>残缺数据：url："+video.getUrl()
		}
		if (arrayList.size >= MAX_LENGTH) {
			saveOrUpdate(arrayList)
			arrayList = new ArrayList<VideoMetadata>()
			if(INSERT_COUNT>=200){
				flag = false
				INSERT_COUNT=0
			}
			Thread.sleep(1000);
		}
	}
	
}
