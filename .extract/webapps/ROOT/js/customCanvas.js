/**
 * Created with IntelliJ IDEA.
 * User: fhuet
 * Date: 02/07/13
 * Time: 13:34
 * To change this template use File | Settings | File Templates.
 */

function customCanvas(src, width,height) {
    this.src = src;
    //this.name = name;
    //debugger;
    this.canvas = document.createElement('canvas'); //name; //document.getElementById(name);
    this.canvas.height=height;
    this.canvas.width=width;
    this.ctx = this.canvas.getContext("2d");
    this.original;// =[];
    this.canvas.onmouseover=transformOthers;
//    this.canvas.onmouseout=reset ;
    this.canvas.onmouseout = resetOthers;


    this.image = new Image();
    this.image.src = src;
    this.image.canvas = this;
    this.canvas.parent=this;


    this.others = [];


    this.image.onload = function () {
        this.canvas.ctx.drawImage(this, 0, 0, 100, 100);
        this.canvas.original = this.canvas.ctx.getImageData(0, 0, 100, 100);
    };

    function transformSelf() {
       var test = this.parent.ctx.getImageData(0, 0, 100, 100);
        for (i = 0; i < test.data.length; i += 4) {

            //  console.log(test.data[i] + "," + test.data[i+1] + "," + test.data[i+2] + "," +test.data[i+3]);
            if (test.data[i] < 150) {
                test.data[i] = 255;
                test.data[i + 1] = 0;
                test.data[i + 2] = 0;
            }
        }
        this.parent.ctx.putImageData(test, 0, 0);
    }

    function transformOthers() {
        var others = this.parent.others;
        for (i in others) {
            //console.log();
            others[i].transform(this.parent.ctx.getImageData(0, 0, 100, 100));
        }
    }


    this._reset = function() {
        if (this.original != null) {
            this.ctx.putImageData(this.original, 0, 0);
        }
    }


    function resetOthers() {
        var others = this.parent.others;
        for (i in others) {
            //console.log();
            others[i].reset();
        }
    }




    customCanvas.prototype.addOther = function (other) {
        this.others.push(other);
    }


    customCanvas.prototype.reset = function() {
       this._reset();
    }

    customCanvas.prototype.transform= function(imgData) {
        var test = this.ctx.getImageData(0, 0, 100, 100);
        //debugger;

        for (i = 0; i < test.data.length; i += 4) {
            //  console.log(test.data[i] + "," + test.data[i+1] + "," + test.data[i+2] + "," +test.data[i+3]);
            if (Math.abs(imgData.data[i]-test.data[i])>50) {
                test.data[i] = 255;
                test.data[i + 1] = 0;
                test.data[i + 2] = 0;
                //alpha
                test.data[i+3] = 10;
            }
        }
        this.ctx.putImageData(test, 0, 0);


    }

}