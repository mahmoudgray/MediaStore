(function($){
    //image sélectionnée
    var selected;
    //la galerie cible
    var galleryToCompare=null;
    //Conteneur des images cibles
    var comparisonContainer;
    //Url su service de comparaison
    var comparisonUrl=null;
    //Url du service de recherche d'images
    var galleryImagesFinderUrl=null;
    //Url des recherche des noms des galeries
    var galleryNamesFinderUrl=null;

        //enregistre le nom de la galerie à comparer 
        var loadGalleryToCompare = function(val){
		galleryToCompare = val;
	};
        window.loadGalleryToCompare=loadGalleryToCompare;
       //fonction d'initialisation des parametres
       var configureGalleryComparator= function(compUrl,galleryIFUrl,galleryNFUrl,CompContainer,galleryNamesLists){
           comparisonUrl=compUrl;
           galleryImagesFinderUrl=galleryIFUrl;
           galleryNamesFinderUrl=galleryNFUrl;
           comparisonContainer=CompContainer;  
           if(galleryNamesLists!=null)galleryNamesLists.loadPaths();
       }
       window.configureGalleryComparator=configureGalleryComparator;
        var  resetSelectedImage = function(){
            selected=null;
        };
        window.resetSelectedImage=resetSelectedImage;
       //enregistre l'image selectionnee
        var loadSelectedImage = function(val){
        selected = val;
	};
         window.loadSelectedImage=loadSelectedImage;
	//Affiche la liste des galeries dans l'element ciblé par la fonction
	$.fn.loadPaths = function(){
		var list = $(this);
		$.get(galleryNamesFinderUrl,function(rep){
				list.empty();
				$.each(rep,function(key, value) {
				  	list.append($('<option></option>').attr('value', value).text(value));
				});
                                list.prop('selectedIndex',-1);
			}
		);
	};
	$.fn.list = function(){
		selected = this;
	};
        var trim=function(){return this.replace(/^\s+|\s+$/g, '');};
        window.trim=trim;
        /**
         *Fonction par défaut qui se charge de comparer une image avec 
         * -les autres images de la base dans "drag n drop"
         * -la galerie choisie dans "Gallery comparator"
         * @param parametres de communication avec le serveur
         */
        var onclickImageToGalery = function(params){
            var imgPath = params.clickedPath;
            // Cette ligne retire les div avec les pourcentages à chaque appel
            $('*').remove('.percent');
            $('*').remove('.selectedFromSrc');
            // Garde les images de tablesimilar avant la suppression
            if(document.getElementById('tablesimilar') != null){
            	$('#tablesimilar').find('img').each(function(){
            		$(this).appendTo('#compare_result');
            	});
            }
            $('*').remove('#tablesimilar');
            var divInitial = comparisonContainer;
            if(divInitial){
            	divInitial.find("img").each(function(){
            		// Chaque image passe à une opacité de 0.65 
            		var img = $(this);
            		img.css({'opacity':'0.7','border':'none','box-shadow':'1px 1px 12px #222'});
            	});
            }            
            //met à jour l'image choisie dans la page "dragndrop"
            if(selected){
                    selected.attr('src',params.src);
             }
             //demande de recherche d'images similaires, si la galerie vaut null la comparaison se ferra avec toutes les images
                    $.get(comparisonUrl+"?path="+imgPath+"&gallery="+galleryToCompare,
                           function(rep){
                                //si il n'y pas de galerie choisie on affiche les images similaires à la base
                                if(!galleryToCompare){
                                    var newparams = $.extend(params,{"images":rep.images});
                                    receiveImages(newparams);
                                }else{
                                //si une galerie est choisie, on met en évidence les images similaires dans la galerie
                                    var divResult = comparisonContainer;
                                    var paths = new Array();
                                    var rmse = new Array();
                                    if(divResult){
                                        divResult.find("img").each(function(){
                                            var img = $(this);
                                            var incr = 0;
                                            // On met d'abord toutes les images à 0.1 d'opacité
                                            img.css({'opacity':'0.5','border':'none','box-shadow':'none'});
                                            $.each(rep.images,function(){
                                            	paths[incr] = this.path;
                                                rmse[incr]=this.rmse;
                                            	incr += 1;
                                            });
                                            divResult.empty();
                                        });   
                                        //en attendant la résolution du bug graphique
                                        //même travail que reveiveImage() sans le passage à displayGallery pour le bind avec les clics.
                                        
                                         for (f in rep.images) {
                                            var image = rep.images[f];
                                            var rmse = (image.rmse);
                                            var template = '<img src="base64Sig:image/jpg;base64,{{base64Data}}" title="{{path}} "/>';
                                            var imgTag = Mustache.to_html(template, image);
                                            divResult.append(imgTag);
                                        }
                                    
                                    
                                    //$('*').animate({scrollTop:0}, 300);
                                    //reorganizeImages(imgPath, paths,distance);
                                    //reorganizeResults(divResult.width());
                                }
                            }
                        }
                    );
                 };
                 
        /**
         * Récupération des images de la galerie sélectionnée, 
         * passage de celles-ci à la fonction de mise en forme.
         */
	$.fn.displaySelectedGallery = function(params){
            var container = $(this);
            if(params.galleryToLoad){
                    container.empty().children().remove();
                    container.css({'text-align':'center'});
                    //Un span avec un loader.gif
                	var spanLoader = $('<span><img src="images/loading.gif"/>Loading images for "'+ params.galleryToLoad +'"...</span>')
                						.css({'font-family':'Trebuchet MS, Arial, serif',
                							  'font-size':'14px',
                							  'color':'black',
                							  'text-align':'center'});
                    container.append(spanLoader);
                    //demande des images de la galerie au server
                    $.get(galleryImagesFinderUrl+"?gal="+params.galleryToLoad,function(rep){
                            if(rep.images.length===0){
                                container.empty().children().remove();
                                //span warning en cas de photo introuvable dans la DB
                                var spanWarning = $('<span><img src="images/warning.png"/>&nbsp;No image found for "'+params.galleryToLoad+'" !</span>')
                						.css({'font-family':'Trebuchet MS, Arial, serif',
                							  'font-size':'14px',
                							  'color':'red',
                							  'text-align':'center'});
                                container.append(spanWarning);
                            }
                            else{
                                var img={"images":rep.images,"containerImgs":container};
                                var newparams = $.extend(params,img);
                                //appel de la mise ne forme
                                receiveImages(newparams);
                        }
                    }
            	);
            }
	};
        /**
         *Fonction d'affichage des images dans le conteneur cible.
         *Elle lie aussi les images avec la fonction de comparaison sur l'événement "click".
         * @param liste des parametres de communication avec le serveur
         * @returns l'objet courant
         */
	$.fn.displayGallery = function(params){
            var img = $(params.image);
            var funct = null
            if(params.fctClick==null){
                funct=onclickImageToGalery;
            }else{
                funct=params.fctClick;
            }
            //lien avec la fonction de comparaison sur click de l'image
             img.click(function(){
                    var clickedPath=$(this).attr("title");
                    var newsparams= $.extend(params,{"clickedPath":clickedPath});
                    funct(newsparams);
                });
            // On appelle les modifications sur les images et la galerie
            //guiImage(img,params,$(this).width());
            $(this).append(img);
           // guiGallery($(this));
            return this;
	};
            /**
             * Mise en forme des images dans des balises <img>
             * @param liste des parametres avec les infos des images 
             * @returns {undefined}
             */
             function receiveImages(params) {
                $(params.imgSelected).empty();
                params.containerImgs.empty().children().remove();
                    for (f in params.images) {
                                var image = params.images[f];
                                var rmse = (image.rmse);
                                var template = '<img src="base64Sig:image/jpg;base64,{{base64Data}}" title="{{path}} "/>';
                                var imgTag = Mustache.to_html(template, image);
                                var description = '<div class="description flt"> Distance:' + rmse + 
                                '<br></a><br></div>';
                                var imgInfos = {
                                        "image":imgTag,
                                        "description": description,
                                        "path":$(imgTag).attr('title'),
                                        "src":$(imgTag).attr('src'),
                                }
                                //attention à l'ordre de l'extend
                                var newparams=$.extend(params,imgInfos);
                                params.containerImgs.displayGallery(newparams);
                    }
                    
            }
	function setTooltipToImg(img,path){
		img.attr('exif','true');
		var text = '<b>Name :</b> ' + getSimpleName(path);
		text += '<br/><b>Subfolder : </b>' + getPath(path);
		text += '<br/><b>Other information : </b>' + printExifData(img,'true','true','true','true','true','true','true','true','true','true','true','true');
                img.tooltip({content:text,show:{effect:"slide",duration:150,direction:'up'},tooltipClass:"tooltipClass"});
	}

	// On passe en paramètre l'image et plein de paramètres exif
	function printExifData($img,$imgdesc,$pxxdim,$pxydim,$datetimeorg,$datetimedgt,$flash,$contrast,$sat,$artist,$cprt,$xres,$yres){
		var res = '<br/>';
		if($imgdesc=='true'){res+='<i>Description : </i>'+ EXIF.getTag($img,'ImageDescription');}
		if($pxxdim=='true'){res+='<br/><i>Pixel X Dimension : </i>'+ EXIF.getTag($img,'PixelXDimension');}
		if($pxydim=='true'){res+='<br/><i>Pixel Y Dimension : </i>'+ EXIF.getTag($img,'PixelYDimension');}
		if($datetimeorg=='true'){res+='<br/><i>Original Date Time : </i>'+ EXIF.getTag($img,'DateTimeOriginal');}
		if($datetimedgt=='true'){res+='<br/><i>Digitized Date Time : </i>'+ EXIF.getTag($img,'DateTimeDigitized');}
		if($flash=='true'){res+='<br/><i>Flash : </i>'+ EXIF.getTag($img,'Flash');}
		if($contrast=='true'){res+='<br/><i>Contrast : </i>'+ EXIF.getTag($img,'Contrast');}
		if($sat=='true'){res+='<br/><i>Saturation : </i>'+ EXIF.getTag($img,'Saturation');}
		if($artist=='true'){res+='<br/><i>Artist : </i>'+ EXIF.getTag($img,'Artist');}
		if($cprt=='true'){res+='<br/><i>Copyright : </i>'+ EXIF.getTag($img,'Copyright');}
		if($xres=='true'){res+='<br/><i>X Resolution : </i>'+ EXIF.getTag($img,'XResolution');}
		if($yres=='true'){res+='<br/><i>Y Resolution : </i>'+ EXIF.getTag($img,'YResolution');}
		return res;
	}

	function guiImage($img,$params,$width){
		setTooltipToImg($img,$params.path);
		$img.fadeIn(1000);
		// Chaque image occupe 45% de son conteneur et avec une marge de 10px
		// On s'assure d'avoir toujours 2 images par ligne maximum
		var pourcentage = (100/($width/250))-1;
		$img.css({'width':pourcentage+'%','margin':'10px','box-shadow':'1px 1px 12px #222'});
	}

	// Alignement de la galerie
	function guiGallery($gal){
		$gal.css('text-align','center');
	}

	// Utilitaire pour récupérer le nom simple de l'image
	function getSimpleName($v){
		var tab = $v.split('/');
		return tab[tab.length-1];
	}

	function getPath($v){
		var tab = $v.split('/');
		return tab[tab.length-2];
	}

	function reorganizeImages($path, $tab,$rmse){
		// On réorganise les images de la source
		var div = $('#compare_source');
		if(document.getElementById('selected') != null){
			var selected = $('#selected');
			var others = $('#others');
			div.find('img').each(function(){
				var img = $(this);
				// Si l'image a un titre : elle n'est pas sélectionnée
				if(img.attr('title')){
					others.append(img);
				} else {
					highlightSelectedImage(selected,img,$path);
				}
			});
			selected.show();
			others.show();
		} else {
		 	var selected = $('<div/>').attr('id','selected').append('Selected image<br/>').hide();
			var others = $('<div/>').attr('id','others').append('<br/>Other images<br/>').hide();
			div.find('img').each(function(){
				var img = $(this);
				// Si l'image a un titre : elle n'est pas sélectionnée
				if(img.attr('title')){
					others.append(img);
				} 
				// Image sélectionnée
				else {
					highlightSelectedImage(selected,img,$path);
				}
			});
			div.empty();
			div.append(selected);
			div.append(others);
			selected.show();
			others.show();		
		}
		// On réorganise les images du résultat
		var div2 = $('#compare_result');
		var valtest = div2.find('img').size();
		if(document.getElementById('similar') != null){
			var similar = $('#similar');
			var different = $('#different');
			div2.find('img').each(function(){
				var img = $(this);
				var bool = 1;
            	var rmse=null;
				for(var i = 0 ; i < $tab.length ; i++){
					if(img.attr('title').trim()==$tab[i]){
						bool = 0;
                        rmse=100-$rmse[i];
                        img.attr('pct',rmse);
						similar.append(img);
					}
					if(bool == 1){
						img.css({'width':'45%','margin':'10px','box-shadow':'1px 1px 12px #222'});
						different.append(img);
					}
				}
			});
			selected.show();
			others.show();
		} else {
		// PB ICI avec similar...............
			var similar = $('<div/>').attr('id','similar').append('Similar images').hide();
			var different = $('<div/>').attr('id','different').append('<br/>Other images<br/>').hide();
			div2.find('img').each(function(){
				var img = $(this);
				var bool = 1;
                var rmse=null;
				for(var i = 0 ; i < $tab.length ; i++){
					if(img.attr('title').trim()==$tab[i]){
						bool = 0;
                        rmse=100-$rmse[i];
                        img.attr('pct',rmse);
						similar.append(img);
					}
					if(bool == 1){
						img.css({'width':'45%','margin':'10px','box-shadow':'1px 1px 12px #222'});
						different.append(img);
					}
				}
			});
			div2.empty();
			div2.append(similar);
			div2.append(different);
			similar.show();
			different.show();
		}
	}

	function highlightSelectedImage($div,$img,$path){
		$div.append($img);
		$img.css({'opacity':'1','border':'solid 2px blue','box-shadow':'6px 6px #00aedb'});
		var span = $('<br/><span><b>Selected image :</b> ' + getSimpleName($path) + '</span>')
        		  .css({'position':'relative',
                  		   'z-index':'999',
                           'height':'20px',
                           'font-family':'Trebuchet MS, arial,serif',
                           'font-size':'14px',
                           'color':'blue',
                           'background-color':'#0d82df',
                           'padding':'10px',
                    	   'border':'1px solid blue'})
                        	.addClass('selectedFromSrc')
                        	.illuminate({
						   		'intensity': '1',
						   		'blink': 'false',
						   		'outerGlow': 'true',
						   		'outerGlowSize': '300px',
						   		'outerGlowColor': 'white'
							});
		$div.append(span);
	}

	function highlightSimilarImage($div,$img,rmse){
		$div.append($img);
        $img.css({'opacity':'1','border':'solid 2px green','box-shadow':'6px 6px #48a627'});
        var pourcentage = rmse;
    	var span = $('<span>'+pourcentage+'%</span>')
                  .css({'position':'relative',
                  		'float':'bottom',
                        'width':'100%',
                    	'z-index':'1',
                        'height':'10px',
                        'font-family':'Trebuchet MS, arial,serif',
                        'font-size':'14px',
                        'color':'white',
                    	'background-color':'#6dc066',
                        'padding-top':'5px',
                        'padding-bottom':'5px',
                        'padding-left':'70px',
                        'padding-right':'70px',
                    	'border':'1px solid green'})
                    .addClass('percent')
                    .effect("bounce", { times:3 }, 900);
		$div.append(span);
	}

	function reorganizeResults(wid){
		var images = new Array();
		var i = 0;
		var tablesimilar = $('<table>').attr('id','tablesimilar').css({'width':'100%','border':'none'});
		$('#similar').find('img').each(function(){
			var img = $(this);
			images[i] = img;
			i++;
			img.remove();
		});
		images.sort(function(a,b){
			return b.attr('pct') - a.attr('pct');
		});
		var tr;
		for(i = 0 ; i < images.length ; i++)
		{
			var finalimg = images[i];
			guiSimilarImage(finalimg);
			setTooltipToImg(finalimg,finalimg.attr('title'));
			if((i%2)==0){
				tr = $('<tr>').css({'width':wid+'px','border':'none','text-align':'center'});
				tablesimilar.append(tr);
			}
			var td;
			if(i==(images.length-1) && (i%2)==1){
				finalimg.css({'width':(((tr.width()/2)*finalimg.attr('pct'))/100)+'px'});
				td = $('<td>').css({'width':wid+'px','border':'none','text-align':'center'});
				td.attr('colspan','2');
			} else {
				td = $('<td>').css({'width':(wid/2)+'px','border':'none','text-align':'center'});
			}
			td.append(finalimg);
			tr.append(td);
			$('#similar').append(tablesimilar);
		}
	}

	function guiSimilarImage(img){
		var rmse = img.attr('pct');
        img.css({'opacity':'1','width':rmse+'%'});
    	var span = $('<span>'+Math.round(img.attr('pct'))+' %</span>')
                  .css({'position':'relative',
                        'width':'40px',
                    	'z-index':'999',
                        'height':'10px',
                        'font-family':'Trebuchet MS, arial,serif',
                        'font-size':'14px',
                        'color':'white',
                    	'background-color':'#6dc066',
                        'padding-top':'5px',
                        'padding-bottom':'5px',
                        'padding-left':'50px',
                        'padding-right':'50px',
                    	'border':'1px solid green'})
                    .addClass('percent')
                    .effect("bounce", { times:3 }, 900);
        setSpanColors(img,span,rmse);
        img.after('<br/>');
		img.after(span);
	}

	function setSpanColors(img,span,rmse){
		if(rmse >= 0 && rmse < 10){
			span.css({'color':'white','background-color':'red','border':'1px solid #c5000b'});
			img.css({'color':'white','background-color':'red','border':'2px solid #c5000b','box-shadow':'6px 6px red'});
		}
		if(rmse >= 10 && rmse < 20){
			span.css({'color':'white','background-color':'#ff8080','border':'1px solid red'});
			img.css({'color':'white','background-color':'#ff8080','border':'2px solid red','box-shadow':'6px 6px #ff8080'});
		}
		if(rmse >= 20 && rmse < 30){
			span.css({'color':'white','background-color':'#ff950e','border':'1px solid #ff420e'});
			img.css({'color':'white','background-color':'#ff950e','border':'2px solid #ff420e','box-shadow':'6px 6px #ff950e'});
		}
		if(rmse >= 30 && rmse < 40){
			span.css({'color':'white','background-color':'#ffd320','border':'1px solid #ff950e'});
			img.css({'color':'white','background-color':'#ffd320','border':'2px solid #ff950e','box-shadow':'6px 6px #ffd320'});
		}
		if(rmse >= 40 && rmse < 50){
			span.css({'color':'black','background-color':'yellow','border':'1px solid #ff950e'});
			img.css({'color':'black','background-color':'yellow','border':'2px solid #ff950e','box-shadow':'6px 6px yellow'});
		}
		if(rmse >= 50 && rmse < 60){
			span.css({'color':'black','background-color':'#ffff99','border':'1px solid #ff950e'});
			img.css({'color':'black','background-color':'#ffff99','border':'2px solid #ff950e','box-shadow':'6px 6px #ffff99'});
		}
		if(rmse >= 60 && rmse < 70){
			span.css({'color':'white','background-color':'#95ccff','border':'1px solid #0099ff'});
			img.css({'color':'white','background-color':'#95ccff','border':'2px solid #0099ff','box-shadow':'6px 6px #95ccff'});
		}
		if(rmse >= 70 && rmse < 80){
			span.css({'color':'white','background-color':'#47b8b8','border':'1px solid #0066cc'});
			img.css({'color':'white','background-color':'#47b8b8','border':'2px solid #0066cc','box-shadow':'6px 6px #47b8b8'});
		}
		if(rmse >= 80 && rmse < 90){
			span.css({'color':'white','background-color':'green','border':'1px solid darkgreen'});
			img.css({'color':'white','background-color':'green','border':'2px solid darkgreen','box-shadow':'6px 6px green'});
		}
		if(rmse >= 90){
			span.css({'color':'white','background-color':'#00513d','border':'1px solid #02261d'});
			img.css({'color':'white','background-color':'#00513d','border':'2px solid #02261d','box-shadow':'6px 6px #00513d'});
		}
	}
})(jQuery);