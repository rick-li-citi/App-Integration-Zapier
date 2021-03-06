import {
    Utils
} from 'symphony-integration-commons/js/utils.service';
import {
    initEnrichers,
    registerEnrichers
} from 'symphony-integration-commons/services/registerEnricher';
import _ from 'lodash';

const dependencies = [
    'ui',
    'extended-user-info',
    'modules',
    'entity',
    'dialogs',
];

const params = {
    configurationId: Utils.getParameterByName('configurationId'),
    botUserId: Utils.getParameterByName('botUserId'),
    host: `${window.location.protocol}//${window.location.hostname}:${window.location.port}`
};

const registerExtension = (config) => {
    const controllerName = `${config.appId}:controller`;
    const uiService = SYMPHONY.services.subscribe('ui');
    console.log('==== dialogs', SYMPHONY.services.subscribe('dialogs'));

    uiService.registerExtension(
        'app-settings',
        config.appId,
        controllerName, {
            label: 'Configure'
        }
    );
}

const registerModule = (config) => {
    const controllerName = `${config.appId}:controller`;
    const controllerService = SYMPHONY.services.subscribe(controllerName)
    
    const modulesService = SYMPHONY.services.subscribe('modules');
    const entityService = SYMPHONY.services.subscribe('entity');
    let msgType = 'com.symphony.integration.zapier.event.v2.searchMessage';
    entityService.registerRenderer(msgType, {}, controllerName);
    controllerService.implement({
        action(){
            const dialogsService = SYMPHONY.services.subscribe('dialogs');
            modulesService.show(
                config.appId, {
                    title: config.appTitle
                },
                controllerName,
                'https://uat.citivelocity.com/analytics/charting3/?allowCross=false', {
                    canFloat: true
                }
            );
            // dialogsService.show('test', controllerName, `
            // <dialog>
            //     <h1>Comment</h1>
            //     <multiline-input id="comment" placeholder="Add your comment..."/>
            //     <div>
            //         <action id="closeCommentDialog" class="tempo-text-color--link"/>
            //         <action id="commentIssue" class="tempo-text-color--link"/>
            //     </div>
            // </dialog>
            // `, {}, {});
            
        },
        render(type, data) {

            if (type == msgType) {
                
                let resultJson = JSON.parse(data.data).Results;
                // console.log('======',resultJson);
                // let resultJson = [];
                let resultML = resultJson.map( article => {
                    return `
                    <card class="barStyle" accent="tempo-bg-color--green" iconSrc="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png">
                        <header>
                            <div>
                                <a class="tempo-text-color--link" href="www.google.com">${_.escape(article.docTitle)}</a>
                                    <span>Author</span>
                                    <span class="tempo-text-color--blue">${article.analyst.join(',')}</span> 
                            </div>
                        </header>
                        <body>
                        <div>
                            <span class="tempo-text-color--secondary">Description:</span>
                            <span class="tempo-text-color--normal">${_.escape(article.docTeaser)}</span>
                        <br/>
                        <img src="${article.coverImageURL}"/>
                    </div>
                    <hr/>
                    </body>
                    </card>`
                });
                // console.log()';
                console.log(`=====<messageML>${resultML.join('')}</messageML>`);
                return {template: `<messageML>${resultML.join('')}</messageML>`, data: {}};
                return {
                    template: `
                    <messageML>
                   
                        <div>
                            <a class="tempo-text-color--link" href="www.google.com">xxxx</a>
                                <span>Author</span>
                                <span class="tempo-text-color--blue">cccc</span> 
                        </div>
                   
                    <div>
                        
                        <span class="tempo-text-color--secondary">Description:</span>
                        
                        <span class="tempo-text-color--normal">dccc</span>

                    <br/>
                    
                </div>
                <hr/>
                <div>
                <action id="assignTo" class="tempo-btn--primary"/>
                <action id="commentIssue" class="tempo-btn--primary"/>
                </div>
                
                
                    
                    </messageML>
                    `,
                    data: {assignTo: {label: 'assign', service: controllerName}, commentIssue:{label: 'comment', service: controllerName}}
                }
                // return {
                //     // template: `<messageML>
                //     //     <div >hello world</div>
                //     //     <iframe height="200" width="400" src="https://uat.citivelocity.com/analytics/charting3/?allowCross=false" />
                        
                //     // </messageML>`,
                //     template: `
                //     <messageML>
                //     <div class="entity" >
                //     <card class="barStyle" accent="tempo-bg-color--green" iconSrc="https://cdn1.iconfinder.com/data/icons/logotypes/32/chrome-32.png">
                //         <header>
                //             <div>
                //                 <img src="http://rick-li.ngrok.io/citibot/apps/citibot/img/bigicons_bigicon_doc.svg.png" class="tempo-icon--document" />
                                
                //                 <a class="tempo-text-color--link" href="www.google.com">hello google</a>
                //                     <span class="tempo-text-color--normal">Subject is  - </span>
                //                     <span>User</span>
                //                     <span class="tempo-text-color--green">action</span>
                                
                //             </div>
                //         </header>
                //         <body>
                //             <div class="labelBackground badge">
                //                 <div>
                //                         <span class="tempo-text-color--secondary">Description:</span>
                //                         <span class="tempo-text-color--normal">xxxxxxxxx</span>
                //                     <br/>
                //                     <span class="tempo-text-color--secondary">Assignee:</span>
                //                         <mention email="racke1983cn@gmail.com" />
                //                 </div>
                //                 <hr/>
                //                 <div>
                //                     <div>
                //                     <img src="https://uat.citivelocity.com/analytics/eppublic/chartingbe/images/a413e76d-0069-432f-9265-e8d3520fb837.png"/>
                //                     </div>
                //                     <div>
                //                         <span class="tempo-text-color--secondary">&#160;&#160;&#160;Epic:</span>
                //                         <a href="http://google.com">google</a>
                //                     <span class="tempo-text-color--secondary">&#160;&#160;&#160;Status:</span>
                //                     <span class="tempo-bg-color--red tempo-text-color--white tempo-token">
                //                         testtesttest
                //                     </span>
            
            
                                    
                //                         <span class="tempo-text-color--secondary">&#160;&#160;&#160;Labels:</span>
                                        
                //                             <span class="hashTag">#ddd</span>
                //                         </div>
                                    
                //                 </div>
                //             </div>
                //         </body>
                //     </card>
                // </div>
                // </messageML>
                //     `,
                //     data: {}
                // };
            }
        },
        link() {
            console.log('====link ====');
        },
        changed(){

        },
        selected(){

        },
        trigger() {
            console.log('====trigger ====');
            modulesService.show(
                config.appId, {
                    title: config.appTitle
                },
                controllerName,
                'https://uat.citivelocity.com/analytics/charting3/?allowCross=false', {
                    canFloat: true
                }
            );


            //   const url = [
            //     `${params.host}/${config.appContext}/app.html`,
            //     `?configurationId=${params.configurationId}`,
            //     `&botUserId=${params.botUserId}`,
            //     `&id=${config.appId}`,
            //   ];

            //   // invoke the module service to show our own application in the grid
            //   modulesService.show(
            //     config.appId,
            //     { title: config.appTitle },
            //     controllerName,
            //     url.join(''),
            //     { canFloat: true }
            //   );
        },
    });
}

/*
 * registerApplication                       register application on symphony client
 * @params       config                      app settings
 * @params       enrichers                   array of Enrichers to be registered in the application
 * @return       SYMPHONY.remote.hello       returns a SYMPHONY remote hello service.
 */
export const registerApplication = (config, appData, enrichers) => {
    const controllerName = `${config.appId}:controller`;

    let exportedDependencies = initEnrichers(enrichers);
    exportedDependencies.push(controllerName);

    const register = (data) => {
        registerEnrichers(enrichers);
        registerExtension(config);
        registerModule(config);

        return data;
    }

    return SYMPHONY.application.register(
        appData,
        dependencies,
        exportedDependencies
    ).then(register);
};