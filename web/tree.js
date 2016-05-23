/**
 * Created by jonathanh on 2/26/16.
 */

var Tree = Tree || {};

Tree.Basic = Tree.Basic || {
    generateList: function(data) {
        var _this = this;
        _this.data = data;
        this._generateList(data, $("#categoryRoot")[0]);
        data.forEach(function(node){
            _this.validateSubtree(node);
        });
    },
    _generateList: function(data, elRoot) {
        var _this = this;
        var ul = document.createElement("ul");

        data.forEach(function(node){
            var li = document.createElement("li");
            li.appendChild(_this.genNodeInfo(node, li));
            if(node.children) {
                _this._generateList(node.children, li);
            }
            ul.appendChild(li);
        });
        elRoot.appendChild(ul);
    },
    genNodeInfo: function(node, li) {
        var _this = this;
        var wrapper = document.createElement("div");
        wrapper.classList.add("treenode-wrapper");
        wrapper.setAttribute("id", "category-node-"+node.id);

        var labelClass = "treenode-label";
        var labelDiv = _this.genNodeProperty("Name", node.item, labelClass, labelClass + "-" + node.id);
        wrapper.appendChild(labelDiv);

        var startDateClass = "treenode-start-date";
        var startDiv = _this.genNodeProperty("Start Date", node.start, startDateClass, startDateClass + "-" + node.id);
        wrapper.appendChild(startDiv);

        var balanceClass = "treenode-balance";
        var balanceDiv = _this.genNodeProperty("Balance", node.balance, balanceClass, balanceClass + "-" + node.id);
        wrapper.appendChild(balanceDiv);

        var budgetClass = "treenode-budget";
        var budgetDiv = _this.genNodeProperty("Budget", node.budget, budgetClass, budgetClass + "-" + node.id);
        wrapper.appendChild(budgetDiv);

        var categoryClass = "treenode-category";
        var categoryDiv = _this.genNodeProperty("Category", node.category, categoryClass, categoryClass+ "-" + node.id);
        wrapper.appendChild(categoryDiv);

        var history = document.createElement("div");
        var historyTxt = document.createTextNode("Start: " + node.start);
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
                input.setAttribute("type", "button");
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
    validateSubtree: function(root) {
        var _this = this;
        console.log("visited: " + root.item + " " + root.id);
        var children = root.children;
        var elem = document.getElementById("category-node-" + root.id);

        if(children) {
            var accum = 0;
            children.forEach(function(child){
                _this.validateSubtree(child);
                accum += child.budget;
            });
            var diff = root.budget - accum;
            if(diff < 0) {
                genStatusNode("Imbalance: $" + (-diff), "category-node-imbalance", "category-node-imbalance-div");
            }
            else if(diff > 0) {
                genStatusNode("Surplus: $" + diff, "category-node-surplus", "category-node-surplus-div");
            }
            else {
                genStatusNode("Balanced", "category-node-balanced", "category-node-balanced-div");
            }
        }
        else {
            genStatusNode("Leaf", "category-node-leaf", "category-node-leaf-div");
        }

        function genStatusNode(text, elemClassName, divClassName) {
            var div = document.createElement("div");
            var divText = document.createTextNode(text);
            div.appendChild(divText);
            div.classList.add(divClassName);
            elem.insertBefore(div, elem.childNodes[0]);
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