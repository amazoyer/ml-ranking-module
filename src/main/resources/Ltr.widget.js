/*******************************************************************************
 * Copyright 2015 France Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
AjaxFranceLabs.LtrWidget = AjaxFranceLabs.AbstractWidget.extend({

        //Variables
        name : 'Activate LTR',
        menuItems : {},
        type : 'ltr',

        // add rq parameter to activate query reranking
        beforeRequest : function() {
                var rq = '{!ltr model=DatafariModel reRankDocs=100 efi.query="'+this.manager.store.get('q').value+'"}';
                this.manager.store.remove('rq');
                this.manager.store.addByValue('rq', rq);
        }
});
