/**
 * Created by jonathanh on 2/26/16.
 */

var Tree = Tree || {};

Tree.Basic = Tree.Basic || {
    generateList: function(data) {
        var _this = this;
        _this.data = data;
        _this._generateList(data, $("#categoryRoot")[0]);
        _this.validateRoot(data);
    },
    _generateList: function(data, elRoot) {
        var _this = this;
        var ul = document.createElement("ul");

        data.forEach(function(node){
            var li = document.createElement("li");
            li.appendChild(_this.genNodeInfo(node, li));
            if(node.budgets[0].children) {
                _this._generateList(node.budgets[0].children, li);
            }
            ul.appendChild(li);
        });
        elRoot.appendChild(ul);
    },
    genNodeInfo: function(node, li) {
        var _this = this;
	
        var nodeID = node.budgets[0].id - 1;

        var wrapper = document.createElement("div");
        wrapper.classList.add("treenode-wrapper");
        wrapper.setAttribute("id", "category-node-"+nodeID);

        var labelClass = "treenode-label";
        var labelDiv = _this.genNodeProperty("Name", node.item, labelClass, labelClass + "-" + nodeID);
        wrapper.appendChild(labelDiv);

        var startDateClass = "treenode-start-date";
        var startDiv = _this.genNodeProperty("Start Date", node.budgets[0].start, startDateClass, startDateClass + "-" + nodeID);
        wrapper.appendChild(startDiv);

        var balanceClass = "treenode-balance";
        var balanceDiv = _this.genNodeProperty("Balance", node.budgets[0].balance, balanceClass, balanceClass + "-" + nodeID);
        wrapper.appendChild(balanceDiv);

        var budgetClass = "treenode-budget";
        var budgetDiv = _this.genNodeProperty("Budget", node.budgets[0].budget, budgetClass, budgetClass + "-" + nodeID);
        wrapper.appendChild(budgetDiv);

        var categoryClass = "treenode-category";
        var categoryDiv = _this.genNodeProperty("Category", node.budgets[0].category, categoryClass, categoryClass+ "-" + nodeID);
        wrapper.appendChild(categoryDiv);

        var history = document.createElement("div");
        var historyTxt = document.createTextNode("Start: " + node.budgets[0].start);
        history.classList.add("category-history");
        history.appendChild(historyTxt);

        history.addEventListener("mousewheel", function(e){
            console.log("hello son");
            e.preventDefault();
        }, true);

        wrapper.appendChild(history);

        var newChild = document.createElement("button");
        var newChildTxt = document.createTextNode("New Child");
        newChild.classList.add("treenode-newchild");
        newChild.setAttribute("type", "button");
        newChild.appendChild(newChildTxt);
        newChild.addEventListener("click", function() {
            $.post("/new-category",{
                "parent": node.id
            }).done(function(data){
                console.log("data: " + data);

                var parsedData = JSON.parse(data);
                _this._generateList([parsedData], li);
            });
        }, true);

        wrapper.appendChild(newChild);
        return wrapper;
    },
    genNodeProperty: function (name, value, className, id) {
        var _this = this;
        var div = document.createElement("div");
        div.classList.add(className);
        div.classList.add("node-property-wrapper");

        var labelDiv = document.createElement("label");
        labelDiv.setAttribute("for", id);
        labelDiv.classList.add("node-property-label");
        labelDiv.innerHTML = name;

        var input = document.createElement("input");
        input.setAttribute("id", id);
        input.setAttribute("type", "button");
        input.value = value;
        input.classList.add("node-property-input");

        input.addEventListener("click", function(){
            if(input.getAttribute("type") == "button") {
                input.setAttribute("type", "text");
            }
            else {
                var match = input.id.match(/^.*-(\d+)$/);
                var budId = match[1];
                var object = cDataIdMap[budId];
                object.budgets[0].budget = input.value;
                console.log("value: " + object.budgets[0].budget);
                input.setAttribute("type", "button");
                _this.validateRoot();
            }
        }, true);

        input.addEventListener("keyup", function(e) {
            var keyEnter = 13;
            if(e.keyCode == keyEnter) {
                var id = input.getAttribute("id");
                var match = /.*-(\d+)$/.exec(id);
                var node = cDataIdMap[match[1]];
                _this.commitChanges(node);
                input.setAttribute("type", "button");
            }
        });

        div.appendChild(labelDiv);
        div.appendChild(input);

        div.style.paddingBottom = "5px";
        div.style.paddingRight = "5px";

        return div;
    },
    validateRoot: function() {
        var _this = this;
        _this.data.forEach(function(node){
            _this.validateSubtree(node);
        });
    },
    validateSubtree: function(root) {
        var _this = this;
        var children = root.budgets[0].children;
        var elem = document.getElementById("category-node-" + (root.budgets[0].id - 1));
        
	if(children) {
            var accum = 0;
            children.forEach(function(child){
                _this.validateSubtree(child);
                var budget = 0 | child.budgets[0].budget;
		accum += budget;
            });
            var diff = root.budgets[0].budget - accum;

	    if(diff < 0) {
		genStatusNode("Imbalance: $" + (-diff), "category-node-imbalance", "category-status-imbalance-div");
            }
            else if(diff > 0) {
                genStatusNode("Surplus: $" + diff, "category-node-surplus", "category-status-surplus-div");
            }
            else {
                genStatusNode("Balanced", "category-node-balanced", "category-status-balanced-div");
            }
        }
        else {
            genStatusNode("Leaf", "category-node-leaf", "category-status-leaf-div");
        }
        console.log("curr budget: " + root.budgets[0].budget + " " + root.item);

        function genStatusNode(text, elemClassName, divClassName) {
            var divResults = elem.getElementsByClassName("category-node-status-div");
            var divText = document.createTextNode(text);
            var div = null;
            if(divResults.length > 0) {
		div = divResults[0];
                var classList = div.classList;
                var statusClass = (classList[0].match(/^category-status.*/))
                                    ? classList[0] : classList[1];
                div.classList.remove(statusClass);
                div.classList.add(divClassName);
                div.firstChild.nodeValue = text;
            }
            else {
                div = document.createElement("div");
                div.appendChild(divText);
                div.classList.add(divClassName);
                div.classList.add("category-node-status-div");
                elem.insertBefore(div, elem.childNodes[0]);
            }
            elem.classList.add(elemClassName);
        }
    },
    computeRemainder: function(child) {

    },
    commitChanges: function(node) {
        alert("node: " + node.item);
        /*var _this = this;

        var str = _this.treeToString();
        $.post("/commit", _this.data);*/
    },
    treeToString: function() {
        function _treeToString(root) {
        }
    },
    scrollTest: function(data) {
        console.log("scroll called: " + data);
    }
};
