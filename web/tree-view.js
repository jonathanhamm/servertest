Tree.Parse = Tree.Parse || {
    data: {},
    breadth: {},
    depth: 0,
    levelZeroPtr: {},
    maxDepth: -1,
    divTree: function(data) {
        var _this = this;
        _this.data = data;

        data.forEach(function(node) {
            _this.updateBreadthDepth(_this, node, 1);
            $("#categoryList").append(_this.divItem(_this, node));
        });
        $.each(_this.breadth, function(k,v) {
            console.log(k + " : " + v);
        });
        _this.leftSibling(_this, data);
    },
    divItem: function(_this, node) {
        var item = node.item;
        var name = item[0];
        var records = item[1];
        var children = node.children;
        return  '<div class="item-outer">' +
                    '<div class="item-inner">' +
                        '<div class="item-name">' +
                            name +
                        '</div>' +
                        records.map(_this.genRecordMarkup).join('') +
                    '</div>' +
                    children.map(function(node){
                        return _this.divItem(_this, node);
                    }).join('') +
                '</div>'
    },
    genRecordMarkup: function(rec) {
        return  '<div class="record">' +
                    '<div class="record-start">' + rec.start + '</div>' +
                    '<div class="record-budget">' + rec.budget + '</div>' +
                    '<div class="record-id">' + rec.id + '</div>' +
                '</div>'
    },
    updateBreadthDepth: function(_this, root, depth)  {
        if(_this.breadth[depth]) {
            _this.breadth[depth]++;
        }
        else {
            _this.breadth[depth] = 1;
        }
        root.children.forEach(function(node){
            _this.updateBreadthDepth(_this, node, depth + 1);
        });
    },
    /* tree plotting */
    positionTree: function(_this, root) {
        if(node) {
            _this.initPrevNodeList(_this, root);

            _this.firstWalk(_this, root, 0)

            //_this.xTopAdjustment = node.xCoord - prelim(node);

            //_this.yTopAdjustment = node.yCoord;

            //return secondWalk(node, 0, 0);
        }
        else {
            return true;
        }
    },
    firstWalk: function(_this, node, level) {
        node.leftNeighbor = _this.getPrevNodeAtLevel(_this, level);
        node.modifier = 0;
        if(_this.isLeaf(node) || level == _this.maxDepth) {
        }
    },
    getPrevNodeAtLevel: function (_this, level) {
        var tempPtr = _this.levelZeroPtr;
        var i = 0;

        while(tmpPtr) {
            if(i == level) {
            }

            i++;
        }
        return null;
    },
    leftSibling: function(_this, node) {
        if(!node.leftSibling) {
           // _this._leftSibling(_this, _this.data, node);
            var root = _this.data;
            for(var i = 0; i < root.length; i++) {
                _this._leftSibling(_this, root[i], node);
            }
        }
    },
    _leftSibling: function(_this, root, node) {
        for(var i = 0; i < root.children.length; i++) {
            if(root.children[i] == node) {
                if(i) {
                    return root.children[i - 1];
                }
                else {
                    return null;
                }
            }
            else {
                return _this._leftSibling(_this, root.children[i], node);
            }
        }
        return null;
    },
    initPrevNodeList: function(_this, root) {
        /*var tmpPtr = root;
        _this.levelZeroPtr = root;
        var list = [];

        while(tmpPtr) {

        }*/

    },
    isLeaf: function(node) {
        return !node.children.length
    }

};

