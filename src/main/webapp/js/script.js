function start() {
    var username;
    var sendButton = document.getElementsByClassName("sendButton");

    $(function () {
        Ps.initialize(document.getElementById('chat'));
        Ps.suppressScrollY = true;
    });

    function addMessageDiv(Text, uname, date) {
        var newDiv = document.createElement('div');
        var dateV = new Date(date);
        newDiv.className = 'message1';
        Text = decodeURIComponent(Text);
        uname = decodeURIComponent(uname);
        newDiv.innerHTML = "<span class = 'nickname'>" + uname + ": " + "</span>"
            + "<img src='/resources/remove.png' class='delete'>" + "<img src='/resources/edit.png' class='edit'>"
            + "<p class='text'>" + Text + "</p>";
        return newDiv;
    }

    addEventListener("keypress", function(e) {
       if(e.keyCode == 13 && $('.message').is(':focus')) {
           send();
       }
    });

    sendButton[0].addEventListener("click", send);
    function send() {
        if(username !== undefined) {
            var message = document.getElementsByClassName("message")[0].value;
            $.ajax({
                url: "http://localhost:8080/ChatServlet",
                method: 'POST',
                data: {
                    type: "add",
                    message: message,
                    date: new Date().getTime(),
                    username: username
                }
            });
        }
        else{
            alert("login pls");
        }

    }

    function addMessage(resp) {
        if (username != undefined) {
            var parsed_resp = $.parseJSON(resp);
            var mainWindow = document.getElementsByClassName("mainWindow")[0];
            var messageDiv = addMessageDiv(parsed_resp.message, parsed_resp.username, parsed_resp.date);
            messageDiv.id = parsed_resp.id;
            mainWindow.appendChild(messageDiv);
            $(messageDiv).hide().fadeIn(150);
            document.getElementsByClassName("message")[0].value = "";
            if(parsed_resp.username === username) {
                messageDiv.addEventListener("mouseover", function () {
                    this.childNodes[1].style.visibility = "visible";
                    this.childNodes[2].style.visibility = "visible";
                });
                messageDiv.addEventListener("mouseout", function () {
                    this.childNodes[1].style.visibility = "hidden";
                    this.childNodes[2].style.visibility = "hidden";
                });
                messageDiv.childNodes[1].addEventListener("click", delReq);
                messageDiv.childNodes[2].addEventListener("click", putReq);

            }
            document.getElementById('chat').scrollTop = document.getElementById('chat').scrollHeight;
            Ps.update(document.getElementById('chat'));
        }
        else {
            alert("please, enter your nickname");
        }
    }

    function putReq() {
        var oldmsg = this.parentNode.childNodes[3];
        oldmsg = oldmsg.innerHTML;

        var req = prompt("EditMessage", oldmsg);
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'PUT',
            data: {
                id: this.parentNode.id,
                newmsg: req
            }
        });

    }

    function delReq() {
        var todel = this.parentNode;
        console.log("delete msg id " + todel.id);
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'DELETE',
            data: {
                id: todel.id
            }
        });
    }

    function addUserDiv(user) {
        var newDiv = document.createElement('div');
        newDiv.className = 'onlineUser';
        newDiv.innerHTML = "<p class='text'>" + user + "</p>";
        return newDiv;
    }

    var setNameButton = document.getElementsByClassName("confirmNameButton")[0];
    setNameButton.addEventListener("click", log_request);
    function log_request() {
        username = document.getElementsByClassName("username")[0].value;
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'POST',
            data: {
                type: "log",
                username: username
            }
        })

    }

    function log(resp) {
        var user = document.getElementsByClassName("usersOnline")[0];
        var oldOnlineList = document.getElementsByClassName("onlineUser");
        if(oldOnlineList.length > 0) {
            while (oldOnlineList.length > 0) {
                oldOnlineList[0].remove();
            }
        }
        var users = JSON.parse(resp).usernames.split(",");
        for(var i = 0; i < users.length; i++) {
            var usr = users[i].replace('[','').replace(']','').replace(' ','');
            if(usr !== "null") {
                var userDiv = addUserDiv(usr);
                user.appendChild(userDiv);
                $(userDiv).hide().fadeIn(150);
            }
        }
    }

    function del(resp) {
        var delDiv = document.getElementById(JSON.parse(resp).id);
        $(delDiv).fadeOut(200, function () {
            delDiv.remove();
        });

    }

    function edit(resp) {
        var editDiv = document.getElementById(JSON.parse(resp).id);
        editDiv.childNodes[3].innerHTML = JSON.parse(resp).newmsg;
        $(editDiv).hide().fadeIn(300);
    }

    (function poll() {
        $.ajax({
            url: "http://localhost:8080/ChatServlet",
            method: 'GET'

        })
            .done(function (resp) {
                console.log(resp);
                if (JSON.parse(resp).type === 'add') {
                    addMessage(resp);
                }
                if (JSON.parse(resp).type === 'log') {
                    log(resp);
                }
                if (JSON.parse(resp).type === 'delete') {
                    del(resp);
                }
                if (JSON.parse(resp).type === 'edit') {
                    edit(resp);
                }
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: OK";
                poll();
            })
            .fail(function () {
                var status = document.getElementsByClassName("rightText")[0];
                status.innerHTML = "Server: Unavailable";
                setTimeout(poll, 5000);
            })

    })();
}
