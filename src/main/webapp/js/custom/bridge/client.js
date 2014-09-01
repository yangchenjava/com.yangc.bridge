Ext.define("Client", {
    extend: "Ext.data.Model",
    fields: [
		{name: "username",   type: "string"},
		{name: "ipAddress",  type: "string"},
		{name: "sessionId",  type: "int"},
		{name: "lastIoTime", type: "string"}
    ]
});

Ext.onReady(function(){
	/** ------------------------------------- store ------------------------------------- */
	var store_clientGrid = Ext.create("Ext.data.Store", {
		model: "Client",
		proxy: {
			type: "ajax",
			actionMethods: {
				create: "POST", read: "POST", update: "POST", destroy: "POST"
			},
			url: basePath + "resource/bridge/getClientStatusList"
		},
		autoLoad: true
	});
	
	/** ------------------------------------- view ------------------------------------- */
	var grid_client = Ext.create("Ext.grid.Panel", {
        renderTo: "client",
		store: store_clientGrid,
		width: "100%",
		height: document.documentElement.clientHeight - 125,
		border: false,
        collapsible: false,
        multiSelect: false,
        viewConfig: {
            stripeRows: true,
            enableTextSelection: true
        },
        columns: [
            {text: "用户名", flex: 1, align: "center", dataIndex: "username"},
            {text: "IP地址", flex: 1, align: "center", dataIndex: "ipAddress"},
            {text: "sessionId", flex: 1, align: "center", dataIndex: "sessionId"},
            {text: "最后读写时间", flex: 1, align: "center", dataIndex: "lastIoTime"}
        ],
        tbar: new Ext.Toolbar({
        	height: 30,
			items: [
		        {width: 5,  disabled: true},
		        {width: 55, text: "重启", handler: restartServer, disabled: !hasPermission("bridge" + permission.SEL), icon: basePath + "js/lib/ext4.2/icons/restart.png"}, "-",
		        {width: 55, text: "刷新", handler: refreshClientGrid, disabled: !hasPermission("bridge" + permission.SEL), icon: basePath + "js/lib/ext4.2/icons/refresh.gif"}, "-",
		        {width: 100, disabled: true},
		        {width: 440, xtype: "label", id: "serverStatus"},
			    {width: 16, height: 16, xtype: "image", id: "serverActive"}
		    ]
        })
    });
	
    /** ------------------------------------- handler ------------------------------------- */
    function refreshClientGrid(){
    	grid_client.getSelectionModel().deselectAll();
    	store_clientGrid.load();
    }
    
    function restartServer(){
		message.confirm("确定要重启mina服务？", function(){
			$.post(basePath + "resource/bridge/restartServer", function(data){
				if (data.success) {
					message.info(data.message);
					serverStatus();
					refreshClientGrid();
				} else {
					message.error(data.message);
				}
			});
		});
    }
    
    function serverStatus(){
    	$.post(basePath + "resource/bridge/getServerStatus", function(data){
			if (data) {
				Ext.getCmp("serverStatus").setText("服务器状态 - IP地址：" + data.ipAddress + "，端口：" + data.port + "，超时时间：" + data.timeout + "秒，连接状态：");
				if (data.active) {
					Ext.getCmp("serverActive").setSrc(basePath + "js/lib/ext4.2/icons/accept.png");
				} else {
					Ext.getCmp("serverActive").setSrc(basePath + "js/lib/ext4.2/icons/exception.png");
				}
			}
		});
    }
    serverStatus();
});
