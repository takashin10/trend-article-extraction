Trend Article Extraction API
========================

## Overview
This is an API package for the trend article extraction. It allows you to expand your own application easily.  
This repository contains a sample project illustrating usage of [Trend Article Extraction API][trend_api] and [Open Sentence/Speech Understanding API][hatsuwa_api]  
by [docomo Developper support][dds].

## Features  

* You can get a list of high-profile article from news articles and blog posts on the web.  
* It has been extracted by the unique trend analysis engine.


## Usage

* Filtered by genre or keyword, you can achieve the information service matched to the preferences of the target.  
* It's to extract articles based on the SNS such as Twitter so you should login on a per-user basis.　　
* Authenticating by docomoID, it's possible to use the more convenient function of recommend.   

* If you would like to know more detailed information, please refer to the [references][references].

## Requirement  

This API enables easy access from Android, iOS and the web. And servers are left out from java. there's also REST support.


## Getting Started

1. Clone or Download zip.  
2. Register [docomo Developper support][dds] (for free) and aquire a API key and client_id,secret.
3. Build and run your projects.


## Contributing

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request


## Samples

#### Trend Article Extract API Sample　　　
![ScreenShot_1](https://devsite-pro.s3.amazonaws.com/contents_file/github_trend_readme_sample1.png)
![ScreenShot_2](https://devsite-pro.s3.amazonaws.com/contents_file/github_trend_readme_sample2.png)  

* Over Android 4.0(Minimum Required SDK:API 14)  
* This sample project is usage of that SDKs.   
	**[Trend Article Extraction SDK for Android(v3.0.1)][trend_sdk]**   


#### CalenderView　　

![ScreenShot_3](https://devsite-pro.s3.amazonaws.com/contents_file/contents_1141121114003.png)
![ScreenShot_4](https://devsite-pro.s3.amazonaws.com/contents_file/contents_0141121114002.png)  

* Plan name is morphological analysis by speech understanding API When a schedule is registered with a calendar.
* That will be used as keywords in the keyword search function of trend article extraction API.
* Retrieved articles will be displayed at the bottom of the calendar.

* Over Android 3.0(Minimum Required SDK:API 11)  
* This sample project is usage of that SDKs.   
	**[Trend Article Extraction SDK for Android(v3.0.1)][trend_sdk]**   
	**[Open Sentence/Speech Understanding SDK for Android(v1.0.1)][hatsuwa_sdk]**  


## Licence

Please make sure to comply with the guidelines in order to use our APIs.  
[Guidline][guidline_ja]  
[MIT](https://github.com/docomoDeveloperSupport/trend-article-extraction/LICENSE)  


## Author

[docomo Developper Support][dds]

[dds]:https://github.com/docomoDeveloppersupport "dds"
[hatsuwa_sdk]:https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=85 "Open Sentence/Speech Understanding for Android(v1.0.1)"
[trend_sdk]:https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=26 "Trend Article Extraction for Android(v3.0.1)"
[hatsuwa_api]:https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=85 "Open Sentence/Speech Understanding API"
[trend_api]:https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=26 "Trend Article Extraction API"
[guidline_ja]:https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy "Guidline"
[references]:https://dev.smt.docomo.ne.jp/?p=docs.api.page&api_docs_id=21#tag01