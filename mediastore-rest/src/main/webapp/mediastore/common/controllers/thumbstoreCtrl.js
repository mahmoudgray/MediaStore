/**
 * Created by mahmo_000 on 28/11/2015.
 */
'use strict';
(function(){
    var app = angular.module('thumbstore.ctrl', []);

    app.controller('ThumbstoreCtrl', ['$scope', '$http', 'FoldersFactory', function($scope, $http, FoldersFactory){
        var app = this;
        $scope.free = [];
        $scope.total = [];

        $scope.chart = new CanvasJS.Chart("memoryChart",{
            title :{
                text: "Memory"
            },   legend: {
                horizontalAlign: "right", // "center" , "right"
                verticalAlign: "center",  // "top" , "bottom"
                fontSize: 15
            },
            data: [{
                showInLegend: true,
                legendText: "Used",
                type: "line",
                xValueType: "dateTime",
                dataPoints: $scope.free
            },
                {
                    showInLegend: true,
                    legendText: "Total",
                    type: "line",
                    xValueType: "dateTime",
                    dataPoints: $scope.total
                }]
        });

        var xVal = 0;
        var yVal = 100;
        var updateInterval = 1000;
        var dataLength = 50; // number


        $scope.updateChart = function(){
            var response = $http.get('rest/hello/monitor');
            response.success(function(data, status, headers, config){
                $scope.free.push({x:data.time, y:data.usedMemory/1024/1024});
                $scope.total.push({x:data.time, y:data.totalMemory/1024/1024});
                $scope.applyRender();
            });

            response.error(function(data, status, headers, config){
                alert("AJAX Request error status = ", status);
            });
        };

        $scope.applyRender = function(){
            if($scope.length > dataLength){
                $scope.free.shift();
                $scope.total.shift();
            }
            $scope.chart.render();
        };


        $scope.getSize = function(){
            var response = $http.get('rest/hello/db/size');
            response.success(function(data, status, headers, config){
                $scope.size = data;
            });

            response.error(function(data, status, headers, config){
                alert('AJAX bad request: status = '+status);
            });
        };
          
        $scope.getPath = function(){
            $.get("rest/hello/db/path", function(data){
                $scope.path = data;
            });
        };

        $scope.getIndexedPaths = function(){
            $http.get('rest/hello/paths')
                .success(function(data, status){
                    $scope.paths = data;
                })
                .error(function(data, status){
                    alert("AJAX BAD REQUEST");
                });
        };

        $scope.shrinkUpdate = function(){
            var folders = FoldersFactory.getSelectedFolders();
            $.get('rest/hello/shrinkUpdate',{
                folder: JSON.stringify(folders)
            }, function(data){});
        };

        $scope.getFolders = function(){
            $scope.folders = FoldersFactory.getSelectedFolders();
        };
        
  
        (function(){
            $scope.getSize();
            $scope.getPath();
            $scope.getIndexedPaths();
            $scope.updateChart();
            setInterval($scope.updateChart, 1000);
        })();
    }]);
})();
