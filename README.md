# Zapier WebHook Integration
The Zapier WebHook Integration will allow you to add an ecosystem of 600+ apps to the Symphony platform. Zapier sends notifications and content to Symphony IMs or rooms from your favorite applications including GMail, Office 365, Trello, HubSpot, Twitter, LinkedIn, and hundreds of other productivity apps.
 
[See Symphony on Zapier here](https://zapier.com/zapbook/symphony/)

## [Build Instructions](#build-instructions-for-the-java-developer)

## How it works
With access to a Zapier account, you can configure Zaps in order to receive notifications on Symphony.
A Zap is a blueprint for a workflow you want to do over and over again automatically. Creating a Zap involves choosing a *trigger* and adding one or more *action* steps.

Symphony supports Zapier **actions** to post messages to Symphony via WebHooks. *Symphony cannot be used as a trigger on Zapier.*

## What formats and events it supports and what it produces
Every integration will receive a message sent in a specific format (depending on the system it ingests) and will usually convert it into an "entity" before it reaches the Symphony platform. It will also, usually, identify the kind of message based on an "event" identifier, which varies based on the third-party system.

You can find more details about entities and the Symphony Message ML format [here](https://github.com/symphonyoss/App-Integrations-Core#the-message-ml-format).

We currently support any action that can be configured via our [Symphony Zapbook](https://zapier.com/zapbook/symphony/) on Zapier. 
There, you can choose an icon and define a message header and body.

The message header and body must follow the rules for Symphony Message ML, which can be accessed [here](https://rest-api.symphony.com/docs/message-format/), although you can safely insert plain text and dynamic trigger app information (like a trigger-related name, title or anything that will translate to text in your message).

### Sample Action
Here we will show you a sample payload from Zapier, the Message ML generated by the Integration Bridge, and the message as it is rendered in Symphony. This payload would be the result of a Zap configured to trigger upon the “card created” event in Trello. 

##### Trello payload

```json
{
  "auth_fields": {},
  "request": {
    "files": {},
    "url": "http://requestb.in/1miwh011",
    "headers": {
      "Content-Type": "application/json; charset=utf-8",
      "Accept": "application/json"
    },
    "params": {},
    "data": "{\"message_content\": \"Test Message Body:\\n* Card Test Trello have just been created\", \"message_header\": \"Test Message Header: Trello card Test Trello created\", \"webhook_url\": \"http://requestb.in/1miwh011\"}",
    "method": "POST"
  },
  "action_fields": {
    "message_content": "Test Message Body:\n* Card Test Trello have just been created",
    "message_header": "Test Message Header: Trello card Test Trello created",
    "webhook_url": "http://requestb.in/1miwh011"
  },
  "action_fields_full": {
    "message_content": "Test Message Body:\n* Card Test Trello have just been created",
    "message_header": "Test Message Header: Trello card Test Trello created",
    "webhook_url": "http://requestb.in/1miwh011"
  },
  "meta": {
    "frontend": true
  },
  "action_fields_raw": {
    "message_content": "Test Message Body:\n* Card {{15919238__name}} have just been created",
    "message_header": "Test Message Header: Trello card {{15919238__name}} created",
    "webhook_url": "http://requestb.in/1miwh011"
  },
  "url_raw": "{{webhook_url}}",
  "zap": {
    "live": true,
    "link": "https://zapier.com/app/editor/15919238",
    "name": "Test Trello!",
    "user": {
      "timezone": "Atlantic/South_Georgia"
    }
  }
}
```
##### Generated Symphony entity

```xml
<messageML>
  <entity type="com.symphony.integration.zapier.event.post_message" version="1.0">
    <presentationML>
      Test Message Header: Trello card Test Trello created<br/>
      Test Message Body:<br/>
      * Card Test Trello have just been created
    </presentationML>
    <entity type="com.symphony.integration.zapier.zap" version="1.0">
      <attribute name="name" type="org.symphonyoss.string" value="Test Trello!" />
      <attribute name="link" type="com.symphony.uri" value="https://zapier.com/app/editor/15919238" />
      <attribute name="live" type="org.symphonyoss.string" value="true" />
    </entity>
    <entity name="action_fields" type="com.symphony.integration.zapier.fields" version="1.0">
      <attribute name="message_header" type="org.symphonyoss.string" value="Test Message Header: Trello card Test Trello created" />
      <attribute name="message_content" type="org.symphonyoss.string" value="Test Message Body:&amp;lt;br/&amp;gt;* Card Test Trello have just been created" />
    </entity>
    <entity name="action_fields_full" type="com.symphony.integration.zapier.fields" version="1.0">
      <attribute name="message_header" type="org.symphonyoss.string" value="Test Message Header: Trello card Test Trello created" />
      <attribute name="message_content" type="org.symphonyoss.string" value="Test Message Body:&amp;lt;br/&amp;gt;* Card Test Trello have just been created" />
    </entity>
    <entity name="action_fields_raw" type="com.symphony.integration.zapier.fields" version="1.0">
      <attribute name="message_header" type="org.symphonyoss.string" value="Test Message Header: Trello card {{15919238__name}} created" />
      <attribute name="message_content" type="org.symphonyoss.string" value="Test Message Body:&amp;lt;br/&amp;gt;* Card {{15919238__name}} have just been created" />
    </entity>
  </entity>
</messageML>
```

##### Message rendered on Symphony

![Sample Action](src/docs/samples/sample_trello_action_rendered.png)


# Build instructions for the Java developer

### What you’ll build
You’ll build an integration module to be used with the [Integration Bridge](https://github.com/symphonyoss/App-Integrations-Core).

If you develop a new integration, to get it up and running you'll also need to add it to the core project's web pom file.

### What you’ll need
* JDK 1.7
* Maven 3.0.5+

### Build with maven
Zapier WebHook Integration is compatible with Apache Maven 3.0.5 or above. If you don’t already have Maven installed you can follow the instructions at maven.apache.org.

To start from scratch, do the following:

1. Build the _App-Integrations-Zapier_ dependencies (so you have them in your Maven local repository):
> [_App-Integrations-Commons_](https://github.com/symphonyoss/App-Integrations-Commons)
2. Clone the source repository using Git: `git clone git@github.com:symphonyoss/App-Integrations-Zapier.git`
3. cd into _App-Integrations-Zapier_
4. Build using maven: `mvn clean install`
