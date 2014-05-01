<style type="text/css">
    #progress * { margin: 0; padding: 0;  }
    #progress { opacity: 0.99999; position: absolute; z-index: 9999; left: 0; top: 0; height: 100%; width: 100%; background: #9DB8D0; font-family: Helvetica Neue, Helvetica, Arial, Verdana, sans-serif; color: #000000; font-size: 20px; line-height: 1; }
    #progress .inner { width: 400px; position: absolute; left: 50%; top: 115px; margin: 0 0 0 -200px; padding: 1em; -moz-border-radius: 16px; -webkit-border-radius: 16px; border-radius: 16px; background: #fff; }
    #progress_status { font-size: 0.75em; color: #808080; display: block; list-style: none; line-height: 1.5; margin-top: 2em; }
    #progress_title {  }
    #progress_bar { border: 1px solid #cacaca; height: 20px; margin-top: 10px; padding: 1px;  }
    #progress_colour { height: 100%; background: #555555; background: #f8f8f8; filter: progid:DXImageTransform.Microsoft.gradient(startColorstr='#606060', endColorstr='#000000'); background: -webkit-gradient(linear, left top, left bottom, from(#606060), to(#000000)); background: -moz-linear-gradient(top, #606060, #000000); }
    #progress_colour .top { background-color: #606060; height: 50%; }
    #progress_colour .bottom { background-color: #555555; height: 50%; }
    #progress_logo { position: absolute; top: 30px; left: 50%; margin-left: -110px; height: 54px; width: 250px; background: url("img/logo/logo_big.png"); }
</style>
<script type="text/javascript">
    var progress_struct = {
        percent: 0,
        ms: 0,
        ms_to_jquery: null,
        start_ms: (new Date()).getTime(),
        completed: {},
        fake_interval: null,
        all_but_cytolite_done: false,
        delay_after_complete: 200,
        help_msg: [
            "<spring:message code='did_you_know_1'/>",
            "<spring:message code='did_you_know_2'/>",
            "<spring:message code='did_you_know_3'/>",
            "<spring:message code='did_you_know_4'/>"
        ],
        init: function(){
        
            var dots = 0;
            var max_dots = 3;
            var dots_delay = 100;
            
            var dots_interval = setInterval(function(){
                var pd = document.getElementById("progress_dots");
                
                if( !pd ){
                    return;
                }
                
                pd.innerHTML = '';
                for(var i = 0; i < dots; i++){
                    pd.innerHTML += '.';
                }
                
                dots = (dots + 1) % (max_dots + 1);
                
                if( document.getElementById("progress").style.display == "none" ){
                    clearInterval(dots_interval);
                }
                
            }, dots_delay);
           
        }
    };
    progress_struct.init();
    
    if( window.navigator.userAgent.search("MSIE") >= 0 ){
    	progress_struct.help_msg = [
    		'Did you know GeneMANIA is much faster, smoother, and better looking on <a target="_blank" href="http://google.com/chrome">Google Chrome</a>?  <a target="_blank" href="http://google.com/chrome">Try it out</a>, and see for yourself!'
    	];
    }
    
    function progress(id){
        var percent = {
            jquery: 10,
            layout: 5,
            networks: 5,
            genes: 5,
            ontology: 5,
            cytolite: 70,
            fake: 5
        };
        progress_struct.completed[id] = true;
       
        
        var pc = document.getElementById("progress_colour");
        
        
        if( id == "fake" && progress_struct.percent >= 90 ){
            return;
        }
        
        if( id != "fake" && id != "cytolite" ){
            var all_but_cytolite_done = true;
            for(var id_i in percent){
                if( id_i == "fake" ){
                    continue;
                }
            
                if( !progress_struct.completed[id_i] && id_i != "cytolite" ){
                    all_but_cytolite_done = false;
                    break;
                }
            }
            
            if( all_but_cytolite_done ){
                var time = progress_struct.ms_to_jquery / percent["jquery"];
                progress_struct.fake_interval = setInterval(function(){
                    progress("fake");
                }, time);
            }
            
            // update also in struct
            progress_struct.all_but_cytolite_done = all_but_cytolite_done;
            
        }
        
        
        if( id == "cytolite" && progress_struct.all_but_cytolite_done ){
            pc.style.width = "100%"; 
            clearInterval( progress_struct.fake_interval );
            
            setTimeout(function(){
                document.getElementById("progress").style.display = "none";
            }, progress_struct.delay_after_complete);
            
        } else {
            progress_struct.percent += percent[id];
            progress_struct.percent = Math.min(progress_struct.percent, 100);
            pc.style.width = progress_struct.percent + "%";
        }
        

    }
</script>