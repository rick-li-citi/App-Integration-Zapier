import 'babel-polyfill';
import { initUnauthenticatedApp } from './initApp';
import config from './config.service';

initUnauthenticatedApp(config, [], () => {
    debugger;
    let uiService = SYMPHONY.services.subscribe('ui');
    let cvControllerService = SYMPHONY.services.subscribe("citibot:controller");
 console.log('==== register 4444 ====')
    // The application service that will handle the filter on UI extensions
    // let cvFilterService = SYMPHONY.services.register("cv:filter");
  
    
    // Displays a button on 1-1 instant messages
    uiService.registerExtension(
      'hashtag',
      'cv-assistant', 
      'citibot:controller', 
      {
        label: 'SearchCV',
        data: { 'datetime': Date() }
      }
    );
    console.log('=== done ===')
   
});






