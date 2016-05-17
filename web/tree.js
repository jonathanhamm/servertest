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
            li.appendChild(_this.genNodeInfo(node));

            if(node.children) {
                _this._generateList(node.children, li);
            }

            ul.appendChild(li);
        });
        elRoot.appendChild(ul);
    },
    genNodeInfo: function(node) {
        var _this = this;
        var wrapper = document.createElement("div");
        wrapper.classList.add("treenode-wrapper");
        wrapper.setAttribute("id", "category-node-"+node.id);

        var addChild = document.createElement("button");
        addChild.setAttribute("type", "button");
        var addChildText = document.createTextNode("Add Subcategory");
        addChild.appendChild(addChildText);
        wrapper.appendChild(addChild);

        var labelClass = "treenode-label";
        var labelDiv = _this.genNodeProperty("Name", node.item, labelClass, node.item+"-"+labelClass);
        wrapper.appendChild(labelDiv);

        var startDateClass = "treenode-start-date";
        var startDiv = _this.genNodeProperty("Start Date", node.start, startDateClass, node.item+"-"+startDateClass);
        wrapper.appendChild(startDiv);

        var balanceClass = "treenode-balance";
        var balanceDiv = _this.genNodeProperty("Balance", node.balance, balanceClass, node.item+"-"+balanceClass);
        wrapper.appendChild(balanceDiv);

        var budgetClass = "treenode-budget";
        var budgetDiv = _this.genNodeProperty("Budget", node.budget, budgetClass, node.item+"-"+budgetClass);
        wrapper.appendChild(budgetDiv);

        var categoryClass = "treenode-category";
        var categoryDiv = _this.genNodeProperty("Category", node.category, categoryClass, node.item+"-"+categoryClass);
        wrapper.appendChild(categoryDiv);


        return wrapper;
    },
    genNodeProperty: function (name, value, className, id) {
        var div = document.createElement("div");
        div.classList.add(className);
        div.classList.add("node-property-wrapper");

        var labelDiv = document.createElement("label");
        labelDiv.setAttribute("for", id);
        labelDiv.classList.add("node-property-label");
        labelDiv.innerHTML = name;

        var input = document.createElement("input");
        input.setAttribute("id", id);
        input.value = value;
        input.classList.add("node-property-input");

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

            console.log("diff: " + diff + " " + (diff > 0));

            if(diff < 0) {
                var deficit = document.createElement("div");
                var deficitTxt = document.createTextNode("deficit: $" + (-diff));
                deficit.appendChild(deficitTxt);
                deficit.classList.add("category-node-deficit-div");
                elem.insertBefore(deficit, elem.childNodes[0]);
                elem.classList.add("category-node-deficit");
            }
            else if(diff > 0) {
                console.log("got surplus: " + diff);
                var surplus = document.createElement("div");
                var surplusTxt = document.createTextNode("surplus: $" + diff);
                surplus.appendChild(surplusTxt);
                surplus.classList.add("category-node-surplus-div");
                elem.insertBefore(surplus, elem.childNodes[0]);
                elem.classList.add("category-node-surplus");
            }
            else {
                elem.classList.add("category-node-balanced");
            }
        }
        else {
            elem.classList.add("category-node-leaf");
        }
    },
    computeRemainder: function(child) {

    },
    commitChanges: function() {
        var _this = this;

        var str = _this.treeToString();
        $.post("/commit", _this.data);
    },
    treeToString: function() {
        function _treeToString(root) {
        }
    }
};