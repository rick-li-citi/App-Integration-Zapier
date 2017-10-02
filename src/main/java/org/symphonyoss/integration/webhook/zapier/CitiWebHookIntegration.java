/**
 * Copyright 2016-2017 Symphony Integrations - Symphony LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.symphonyoss.integration.webhook.zapier;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.client.SymphonyClientFactory;
import org.symphonyoss.client.model.Chat;
import org.symphonyoss.client.model.Room;
import org.symphonyoss.client.services.MessageListener;
import org.symphonyoss.integration.exception.RemoteApiException;
import org.symphonyoss.integration.model.message.Message;
import org.symphonyoss.integration.model.message.MessageMLVersion;
import org.symphonyoss.integration.service.StreamService;
import org.symphonyoss.integration.webhook.WebHookIntegration;
import org.symphonyoss.integration.webhook.WebHookPayload;
import org.symphonyoss.integration.webhook.exception.WebHookParseException;
import org.symphonyoss.integration.webhook.parser.WebHookParser;
import org.symphonyoss.integration.webhook.zapier.parser.ZapierParserResolver;
import org.symphonyoss.symphony.clients.model.SymMessage;
import org.symphonyoss.symphony.pod.model.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.symphonyoss.integration.core.properties.IntegrationBridgeImplProperties.USER_POSTED_MESSAGE;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.core.MediaType;

/**
 * Implementation of a WebHook to integrate Zapier to Symphony.
 *
 * Created by ecarrenho on 22/09/16.
 */
@Component
public class CitiWebHookIntegration extends WebHookIntegration {

	@Autowired
	private ZapierParserResolver parserResolver;

	@Autowired
	private CitiBotParserFactory parserFactory;

	private SymphonyClient symClient;

	@Autowired
	private StreamService streamService;

	@Override
	public void onCreate(final String integrationUser) {
		System.out.println("=== " + integrationUser);
		super.onCreate(integrationUser);
		// this.integrationUser = integrationUser;
		String keystorePassword = System.getProperty("keystore.password");
		try {
			symClient = SymphonyClientFactory.getClient(SymphonyClientFactory.TYPE.BASIC,
					"innovate.citibot@symphony.com", // bot email
					"/Users/kl68884/projects/symphony/App-Integrations-Universal/certs/innovate.citibot.p12", // bot
																												// cert
					keystorePassword, // bot cert/keystore pass
					"/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/jre/lib/security/cacerts/", // truststore
																													// file
					"changeit"); // truststore password
			this.symClient.getMessageService().addMessageListener(new MessageListener() {

				@Override
				public void onMessage(SymMessage message) {

					String searchKey = "search";
					String msgText = message.getMessageText();
					System.out.println(msgText);
					String streamId = message.getStreamId();
					Stream stream = new Stream();
					stream.setId(streamId);
					Message ackMessage = new Message();
					ackMessage.setMessage("<messageML>acked</messageML>");
					ackMessage.setData("{}");
					ackMessage.setVersion(MessageMLVersion.V2);
					try {
						postMessage(integrationUser, streamId, ackMessage);
					} catch (RemoteApiException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					if (StringUtils.containsIgnoreCase(msgText, searchKey)
							&& StringUtils.containsIgnoreCase(msgText, "citibot")) {
						String searchContent = StringUtils.substringAfterLast(msgText, searchKey).replaceAll("#", "")
								.trim();
						System.out.println(searchContent);
						try {
							String responseStr = CitiWebHookIntegration.this.searchCVContent(searchContent);
							// WebHookParser parser = getParser(input);
							WebHookPayload payload = new WebHookPayload(null, null, responseStr);
							WebHookParser parser = parserFactory.getParser(payload);
							Message outputMsg = parser.parse(payload);
							ObjectMapper mapper = new ObjectMapper();
							JsonNode newData = mapper.readTree(outputMsg.getData());
							JsonNode searchMessageNode = ((ObjectNode) newData.get("citiSearchMessage")).put("data",
									responseStr);
							((ObjectNode) newData).put("citiSearchMessage", searchMessageNode);
							outputMsg.setData(newData.toString());

							// CitiWebHookIntegration.this.
							// SymMessage aMsg = new SymMessage();
							// aMsg.setFormat(SymMessage.Format.MESSAGEML);
							// aMsg.setMessage(outputMsg.getMessage());

							// citiBridge.postMessage(integrationUser, streamId, outputMsg);
							// symClient.getMessagesClient().sendMessage(stream, aMsg);
							// outputMsg.setData(responseStr);
							Message sentMsg = postMessage(integrationUser, streamId, outputMsg);
							System.out.println(sentMsg.getData());
							;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			});
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public Message postMessage(String integrationUser, String stream, Message message) throws RemoteApiException {
		Message messageResponse = streamService.postMessage(integrationUser, stream, message);
		// LOGGER.info(logMessage.getMessage(USER_POSTED_MESSAGE, integrationUser,
		// stream));

		return messageResponse;
	}

	public String searchCVContent(String searchContent) throws Exception {
		@SuppressWarnings("deprecation")
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String searchContentB64 = Base64.getEncoder().encodeToString(searchContent.getBytes("utf-8"));
		String urlOverHttps = String.format(
				"https://uat.citivelocity.com/hubsearch2/json?q=%s&qencode_schema=base64&start=1&end=10&pubType=research,commentary&platformID=1&model=nonbetamodel-04282017&enrich-facet=Company,Author,Region,EMRegions",
				searchContentB64);
		// CookieStore cookieStore = new BasicCookieStore();
		// BasicClientCookie cookie = new BasicClientCookie("SMSESSION",
		// "aHTj666eWG/eYc2WMWkDVIZLua4kJ7HCLF0JUJFD7eE1OhgXV4sP6Cxj8yQeEjKqZcGfAdKnjjUUEFgmqnBXs6uQKfXSlHMAsGCCexcpUt307pTrA5XW/OFrm6HbGSFL+scSmsy+hBjwdY28BnIu1LlTB9+Fd4fs1OW2RZ1qqTifwQN7RlDt+mStNeMv9rjpo+RrpHdbyfcyHlkRnTogp+gK+TWbKdm2PJAd1tYRXu/p05CXZWATFQ15ifOJI/cIJqS1fsjqKZBNxwJWBbTkG++UwM9l3ecIp02DrnUPTkPUDZTyw46xrQXi3KdfV7KpWLYTLsT1Zlfy4cTuYDrwYVWfU6m6yB7smpej4Ao2PPGgRNFBADEmBtEYWeLyZo2QBRK3hko8QCZqzYyUUfvRheC4SeeuCAU4VRLVPZcgB8+CQpjrj59/xP9Fqv/Tyi1dYErtfkVAu3LlUQ60V/VloFvXgXIbt5nqGoTJKUkRDYliOgfyymvsJU9xEaOe6CLjcEmaiACavBhxLGfa3MTDyPYQ4RzrO2svLFS+Ty8/0df36F3VwLof/IopY+GGBEt13L+YMJufuCeA81bjflDvNfbicOeUoU6wjAobPrvzgL2I9un7BWGwUKI53Ip/qMEveHU8fgN55BA5vmYotX4rTWjLNlDEYzc3TGAwUyg5Cabvxhd50iZ0W0zwACvnmFNyNbqklWH4/+iJvo21GmWNwbyR8tmfSGFboZ1IMOX3RdSawGjiyZKMXcdQ9wR0FDfPXAst1jWiG17XsyWENS80UQ8xcXCuEL3yUwCWlHRSlVGKR2iHbnohY7w1L8jFiMwcVeEsMPxdgR8NPZLzKRZUKTChE0IWsl9d1FtE2hyXaOG4jzwi2YMBXpnbLNIDNchqQkKXFlkK0tdZHY6XpZFgT0Nw7BgKG6oSOTHoiyJMKtT6c/g0mYphx+823A0g9KWzK8pEEZEg4ertdWTFMZPTIKT4uCGty3yjAEKEb9taXaXr0RtO6veQh1krRpljN8jqkcKS7qpnFAxgF0fdHSwS16fm9MXOc860TrumU6pv/QjfO4Cm6DsoDCJYgXg6o1l/");

		HttpGet getMethod = new HttpGet(urlOverHttps);
		getMethod.setHeader("Cookie",
				"cv_device_id=n3yo68BHKYvGWhy8nYYjuw4edZmkrcXFz2BypimbURij94QZv5GB39wGSWfVLcPMFqHp56gYTlQ; cp-rememberme=1mv8wGnOsE6e-SKoRABRtOaPdqajOzPJ37vPtjZVPFRO6_KlJeYBI2A; cv_device_id_uat=n3yo68BHKYt0N9f8iNP3zr4uKV-gbyxeHCYxyqri68dnqnHGmnLwJAhzcYlX1Dw_; _ga=GA1.2.2043630629.1506452169; _pk_id.21.9c66=03d908139ad910f5.1506454344.4.1506713602.1506713532.; x-citiportal-ua-track=p3lKL5s9ErS2LBCTdw5EXDYP6TsW; cp-rememberme-uat=1RB_v79DNzOpWd9r0tWAazx3leojmqsYzbKBsWxnFjaxBpKSyaqnETg; cp-wl=WNTvZRhBGWTgyFNvp1mGnTBR02pJFwTvy5LTpyr10JXDfRJwhbfg!1432583873!-2046532815; locale=null; SMIDENTITY=Yiq+pixWUCmxA4sbtWXohOpXJ7VpeaeD0r19fUuOeW2YR1q/Q2TQJox5UAaqRWT88maDSbigMSHHDwIYo08h/Y4tmv6SDkHVggdfSypXrNowAEOCr7hCXJuE31qYMkc9Mg32r6XAI0UNClYei2chfyJjCTCLVOPup0C45Zgrrq0Tw06ZO1WypouETHUHOm4GR4D6AS8O+ACUq8gtBgrvo25irgLhh0j+A3Gst9vX+Q2XdMpXFls162mXc8VQboTLv7G20URpHCYZ7Ef9b+BuXqiqGQlJSwsztEzW6J5VWSlmx8rQxa+f3IP79acXFIhnM1I50DsgmmlK84+QhDiYfcZbfSYReUWTwJSk2OmctDnAml48YQerghg+9rRtWlz1tjBQWWoiEvL+HllMQjPDo2ndTsxpr2KNqMbmNj/JWzHwRqv/0LZpDvhEQW3OEuTA6Sz2PV17B+qwZxxJzCVAkPvebgUb0LHgvZ8lbC3eiXPPKHodDMo0rWtlt0Y6Gih3/ugE2BRe4vmtuLyvOm4OlYr06uB9WA/52kY0ybcKDmYSeEJku+RZAz322sd+XUFqxQDDrsn5a2qmtBRUHEJwBbGW1uRcsDZExCVE+shXiMU1f/vzdjOZwdyzQtYNq8v/saJp94pSUR90gz7lWw/zhLvrl/SY5g3dpqnQBChv+NTyZBrQlB04D6SoT2gw8k+1; x-citiportal-pageid=MarketBuzz-1506894244626; __stop_IE_second_refresh=FALSE; _pk_ses.10.9c66=*; _pk_id.10.9c66=c63d8fb938c85d34.1506454344.5.1506894245.1506894245.; RT=\"dm=citivelocity.com&si=31e14cc7-1cd7-43ad-8139-49ea8699101a&ss=1506894241321&sl=1&tt=2424&obo=0&sh=1506894244973%3D1%3A0%3A2424&bcn=%2F%2F36cc248b.akstat.io%2F\"; SMSESSION=+OS9qo0bSb2qNneDLAgCq10Kt+p8Y1/XGuZV5Sc0UVU2aB4rwGCEBQPl4AUYHoIJxGN1qTN2GSz8MTQdfm28fmqwtZuvkKTwfkfSbHvHg0y4bLfV7wrPk6YRmmr/LMvYjjTUBtVYrt6sJP014/ZWSmzjeaG72rqYazCOlgievi+wGFtkCioDtTYDk8AA2KpgKfy+Rhd9CDU44xzckuhLnA+3Ug1leDRB+l3CEo3fleYnxs2gFo4o8YcQk3dDB448+wODHk4AwOwFhBXoMzGWLE0t8NFni7QCqosyLtwYOCQRLZ0ona8PDxfJXx2GAFEOABXKrPqPiXFmd7Y12jmvRNRL3cI8v/qkUs5FkcAeoVYb9aSe9Zo4vbtAkHjF5tTJlUK4pFcDoDJqsPq9blyt91fg8XlpVC/Dvexk/rVWmsS8ACwcWWuoIdTzbyNjxnvyIp44aIv+h4orIqwqJZNQQoJqUy9vkrebH8Pd+vTohyAYXbZSc1OdjbqZxJq7lxdYpW3oUzVb034xgf2s+l9xXhZdBlHimburTBDJAiYNmypMNXdYllkufhianSMKp4VofSWF+eCyX8TDBOnj6yPucFIBfpKmApQJuMkCCgREyAVL+BlZ6Fn3J1o8nJqt7z4lo6MQTt6l7bRP80PW5RCPs8H028O6q7YZSqTBPI9F9QE/2owodg+G9bPZuWG/CLJznbCCf0haeQxo1d0wTJqeIj4+TKWUcIKbpwZKVpRRszN2JZ3Iq6PeTyv6ee+T/UqDJCtqdDmNJ29+6uo7Oq4amROwfmMMFu9Hst7+bpcyAm3lLrqDx1O9qHKnWqH5At767kCJq77CCbYcsmF0PLuw2uwr3BeFnezFx98rP4j1U+yrCnyaTROEc9OltEqGzL7FlJFJe1NH9GF7DpdOdcXgWoWtwgFhm5MHI36p5wk0acNOClpoW7Lj256ZEWZAPQ3cEjM6OgI9p55wKsYEjpvJ4eLnYkzExTsgUOqXMoSCUpPErOyZutQQytTzFtXHalgK/bOHK2BAsdCmRWqro/FkA/u0qbRZuZSuYCTNFPGyQd3a0taaFNGUGDZGQFgdVyKp;");
		HttpResponse response = httpClient.execute(getMethod);
		HttpEntity entity = response.getEntity();
		String responseString = EntityUtils.toString(entity, "UTF-8");
		System.out.println(responseString);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode actualObj = mapper.readTree(responseString);
		JsonNode results = actualObj.get("Results");
		if (results.isArray()) {
			for (final JsonNode objNode : results) {
				System.out.println(objNode.get("docTitle"));
			}
		}
		return responseString;
	}

	/**
	 * Parser method for the incoming Zapier payloads.
	 * 
	 * @param input
	 *            Incoming Zapier payload.
	 * @return The messageML resulting from the incoming payload parser.
	 * @throws WebHookParseException
	 *             when any exception occurs when parsing the payload.
	 */
	@Override
	public Message parse(WebHookPayload input) throws WebHookParseException {
		// WebHookParser parser = getParser(input);
		WebHookParser parser = parserFactory.getParser(input);
		return parser.parse(input);
	}

	/**
	 * Get the Zapier Parser based on the event.
	 * 
	 * @param payload
	 *            Payload received by the webhook
	 * @return Specific zapier parser to handle the event or a default parser if no
	 *         specific parser found.
	 */
	private WebHookParser getParser(WebHookPayload payload) {
		return parserResolver.getFactory().getParser(payload);
	}

	/**
	 * @see WebHookIntegration#getSupportedContentTypes()
	 */
	@Override
	public List<MediaType> getSupportedContentTypes() {
		List<MediaType> supportedContentTypes = new ArrayList<>();
		supportedContentTypes.add(MediaType.WILDCARD_TYPE);
		return supportedContentTypes;
	}
}
