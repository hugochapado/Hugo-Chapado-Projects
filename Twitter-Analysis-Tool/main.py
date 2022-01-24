import tkinter as tk
import tkinter.ttk as ttk
import pandas as pd
import webbrowser
import time


##////////////////////////////////////
##-----------READ ME------------------
## Easiest way for Gui to interact with other classes or functions
## is to call them in the section at line 331. Gui can pass the search parameters
##////////////////////////////////////

class Gui:
    ## Citation regarding the GUI class, the trick to get rounded borders on widgets was found on stackoverflow
    ## here: https://stackoverflow.com/questions/51425633/tkinter-how-to-make-a-rounded-corner-text-widget
    ## in the answer from Bryan Oakley

    def __init__(self):

        ## Window setup
        self.root = tk.Tk()
        self.root.geometry("800x600")
        self.root.configure(background="white")
        self.root.title("Influence Analysis")

        ##//////////////////////////////////////////////////////////////
        ##-------------------Base64 encoded images----------------------
        ## Base64 encoded image of rounded blue border used as a style
        self.blueRoundBorderImage = tk.PhotoImage("blueRoundBorderImage", data="""
        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
        """)

        ## Encoded white border
        self.whiteRoundBorderImage = tk.PhotoImage("whiteRoundBorderImage", data="""
        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
        """)

        ## Encoded twitter logo image
        self.twitterImage = tk.PhotoImage("twitterImage", data="""
        iVBORw0KGgoAAAANSUhEUgAAAQAAAAEACAYAAABccqhmAAAABGdBTUEAALGP
        C/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3Cc
        ulE8AAAABmJLR0QAAAAAAAD5Q7t/AAAAB3RJTUUH5AMXBywxps+b0wAAErFJ
        REFUeNrt3XvUXfOdx/H3TlySEpdNUsqqW2kwZSjq0syiSt0v2edXdRuXGjF0
        WVGUqVqlVRbTxqW0Y3W1ocYUe2/XmaaKuldRpULcJlFMlOBHIojcfvPH3uFJ
        PEmec55zzu/s3/681rLwPOc5z2ef5+zv2Zfv7/cDERERERERERERERERERER
        ERERERERERERERERERERERERERERERERERERERERERERERERERERERERERER
        ERERERERERERERERERERERERERERERERERERERERERERERERERERERERERER
        ERERERERfyLfAaQeTGZXADYh4gvApsAGwKeBNYCVy4ctBGYDbwKvAtOAKTie
        Shux9b0NIVIBkI4wmY2I2BjYD9gD2BFYq8Wnmwc8D9wDTMJxd9qI3/e9jSFQ
        AZC2Mpldi4jDgSOA7ejMe2wmcDMwEcd9aSN2PbDdw9NG/IHvHM1SAZC2MLnd
        BDgNOBJYpYu/+klgAo7fpI14bte3O7NrEHEm8GqaxJd1+/cPlgqADIrJ7DpE
        nAMcA6zkMcoLwNk4bujGEYHJ7DAixgHfpTi12ThN4pc9bn9LVACkJSazKxDx
        LeBcYDXfefq4HzgxTeKnOrTdqxBxLHAGsF755Uk49kkbse9tb1qwBcBkdi3g
        rSr+UXqdye1mwERgZ99ZlmIO8AMcF6WNeEGbtnkUcDxwErDOEt/eO03i3/ne
        6Fas4DtAJ5jMrkjE3cD3gFt95wmFySxEHApcCYzwnWcZhgHnE/FVk9vD0yR+
        rcXtjYjYERgHfB0Y3s/DHsfxe98b3KogjwBMbo8BfgW8C/xTmsRP+M5UdSaz
        Q4m4ADgVGOI7TxNeAQ5Ok/ixAW4nRGwIHEJxQXMLlr2fHJQm8S2+N7JVwRWA
        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
        1i74AAAAAElFTkSuQmCC
        """)
        ##--------------END OF BASE64 ENCODED IMAGES--------------------
        ##//////////////////////////////////////////////////////////////

        ##/////////////////////////////////////
        ##---------------Styles----------------
        ## Turn blue rounded edge border image into style
        self.style = ttk.Style()
        self.style.element_create("blueRoundBorder", "image", "blueRoundBorderImage", border=16)
        self.style.layout("blueRoundBorder", [("blueRoundBorder", {"sticky": "nsew"})])
        self.style.configure("TLabel", borderwidth=0, background="#00AEF5")

        ## Turn white rounded edge border image into style
        self.style.element_create("whiteRoundBorder", "image", "whiteRoundBorderImage", border=16, sticky="nsew")
        self.style.layout("whiteRoundBorder", [("whiteRoundBorder", {"sticky": "nsew"})])
        self.style.configure("TEntry", borderwidth=0)

        ## Radio button style
        self.style.configure("TRadiobutton", background="white", font=('Consolas', 11))
        ### Search button style
        self.style.configure("TButton", font=('Helvetica Neue', 20, 'bold'))
        ##/////////////////////////////////////

        ##//////////////////////////////////////////////////////////
        ##----------Main Class Functionality-----------------------
        ##----------Variables---------------------
        self.username = tk.StringVar()
        self.maxDegSeparation = tk.StringVar()
        self.minNumFollowers = tk.StringVar()
        self.searchType = tk.StringVar()
        self.timeElapsed = tk.StringVar()

        ##----------Title-------------------------
        ## Create title frame using blue rounded edge image style
        self.titleFrame = ttk.Frame(style="blueRoundBorder", padding=20, width=500, height=100)
        self.titleFrame.place(x=10, y=10)

        ## Title of project placed into title frame
        self.title = ttk.Label(self.titleFrame, text="Twitter Influence Analysis Tool",
                               font=('Helvetica Neue', 20, 'bold'))
        self.title.place(x=0, y=0)

        ## Group members place into title frame
        self.authors = ttk.Label(self.titleFrame,
                                 text="Written by Hugo Chapado Cruz, Gonzalo Prats Juliani, and Benjamin Zipes",
                                 font=('Helvetica Neue', 10))
        self.authors.place(x=0, y=40)

        ##----------Logo-------------------------
        ## Place twitter image in window
        self.image = tk.Label(self.root, image="twitterImage")
        self.image.place(x=528, y=0)

        ##----------Search Parameters-------------
        self.searchParam = tk.Label(self.root, text="Search Parameters", font=('Consolas', 14, 'bold'),
                                    background="white")
        self.searchParam.place(x=25, y=115)

        ## Entry box for username
        self.usernameLabel = tk.Label(self.root, text=">>> username: ", font=('Consolas', 14), background="white")
        self.usernameLabel.place(x=25, y=145)
        self.searchFrame = ttk.Frame(style="whiteRoundBorder", padding=10)
        self.searchFrame.place(x=160, y=140)
        self.searchEntry = tk.Entry(self.searchFrame, textvariable=self.username, font=('Consolas', 12),
                                    highlightthickness=0, relief="flat", width=30)
        self.searchEntry.pack()

        ## Entry box for max degrees of separation
        self.separation = tk.Label(self.root, text="> maximum degrees of separation: ", font=('Consolas', 11),
                                   background="white")
        self.separation.place(x=25, y=195)
        self.separationFrame = ttk.Frame(style="whiteRoundBorder", padding=10)
        self.separationFrame.place(x=285, y=185)
        self.separationEntry = tk.Entry(self.separationFrame, textvariable=self.maxDegSeparation, font=('Consolas', 12),
                                        highlightthickness=0, relief="flat", width=3)
        self.separationEntry.pack()

        ## Entry box for minimum number of desired followers
        self.followers = tk.Label(self.root, text="> minimum number of followers: ", font=('Consolas', 11),
                                  background="white")
        self.followers.place(x=25, y=240)
        self.followersFrame = ttk.Frame(style="whiteRoundBorder", padding=10)
        self.followersFrame.place(x=270, y=230)
        self.followersEntry = tk.Entry(self.followersFrame, textvariable=self.minNumFollowers, font=('Consolas', 12),
                                       highlightthickness=0, relief="flat", width=10)
        self.followersEntry.pack()

        ##----------Search Type (BFS, DFS) Radiobuttons-------------
        ## Variable searchStyle will contain either "BFS" or "DFS" depending on the button selected
        self.rButton1 = ttk.Radiobutton(self.root, text="BFS Style", value="BFS", variable=self.searchType)
        self.rButton2 = ttk.Radiobutton(self.root, text="DFS Style", value="DFS", variable=self.searchType)
        self.rButton1.place(x=400, y=190)
        self.rButton2.place(x=400, y=220)

        ##----------Results Box-------------------
        self.resultsFrame = ttk.Frame(style="blueRoundBorder", padding=10, width=780, height=235)
        self.resultsFrame.pack(side="bottom", pady=10)
        self.resultsFrame.pack_propagate(0)
        self.results = ttk.Label(self.resultsFrame, text="Results", font=('Helvetica Neue', 20, 'bold'))
        self.results.place(x=0, y=0)

        ##---------Time elapsed-------------------
        self.timeLabel = tk.Label(self.root, textvariable=self.timeElapsed, font=('Consolas', 8), background="white")
        self.timeLabel.pack(side="bottom")

        ##---------Search Button------------------
        self.searchButton = ttk.Button(self.root, text="Search!", command=self.search)
        self.searchButton.pack(side="bottom")

        ## Prevent resizing of window to keep everything looking nice
        self.root.resizable(False, False)
        self.root.mainloop()

    # ///////////////////////////////////////////////////////////

    ##///////////////////////////////////////////
    ##-------On click of Search! button----------
    ## Example functionality: on click of search button, pass to function in other class, then print results of other function

    def search(self):

        ## initialize results list/tuple
        results = []
        followers = []

        ## Destroy any existing search results to make room for new results
        for widget in self.resultsFrame.winfo_children():

            if isinstance(widget, tk.Label):
                widget.destroy()

        ## Store search parameters
        user = self.username.get()
        separation = self.maxDegSeparation.get()
        followers = self.minNumFollowers.get()
        search = self.searchType.get()
        ##//////////////////////////////////////////

        ##//////////////////////////////////////////////////////////
        timeStart = time.time()
        ##--------Interface with other Class or Function------------
        ## Send search parameters to other function, class, etc. to do something with them
        ## -------Example of calling function------------
        results, followers = GraphObject.finalfunction(user, separation, followers, search)

        # <- call functions here, pass search parameters (user, separation, followers, search)

        self.timeElapsed.set("Time Elapsed: " + str(time.time() - timeStart))
        ##//////////////////////////////////////////////////////////

        ##////////////////////////////////////////////
        ##--------Output results to window------------
        ## Displays results of search and search parameters in results frame
        ## Currently takes tuple of account usernames

        ## Display search parameters
        searchParam = tk.Label(self.resultsFrame, text="Search Style: " + self.searchType.get() +
                                                       "\nMax Degrees Separation: " + self.maxDegSeparation.get() + "\nMin Number Followers: " +
                                                       self.minNumFollowers.get(), font=('Helvetica Neue', 10),
                               background="#00AEF5", justify="right")
        searchParam.pack(anchor="ne")

        ## Display results if results list has values
        if results:
            first = True
            for account, followersCount in zip(results, followers):
                if first:
                    first = False
                    label = tk.Label(self.resultsFrame,
                                     text="username: " + account + "\nwith " + str(followersCount) + " followers",
                                     font=('Helvetica Neue', 8), background="#00AEF5", justify="center")
                    label.pack(side="left", fill="x", expand=True, ipady=40)
                    link1 = tk.Label(label, text="@" + account, font=('Helvetica Neue', 10), fg="blue", cursor="hand2",
                                     justify="center")
                    link1.pack(side="top")
                    link1.bind("<Button-1>",
                               lambda a, url="https://www.twitter.com/" + account: webbrowser.open_new(url))
                    continue

                follows = tk.Label(self.resultsFrame, text="follows", font=('Helvetica Neue', 6), background="#00AEF5",
                                   justify="center")
                follows.pack(side="left", fill="x", padx=10, expand=True)
                label = tk.Label(self.resultsFrame,
                                 text="username: " + account + "\nwith " + str(followersCount) + " followers",
                                 font=('Helvetica Neue', 8), background="#00AEF5", justify="center")
                label.pack(side="left", fill="x", expand=True, ipady=40)
                link1 = tk.Label(label, text="@" + account, font=('Helvetica Neue', 10), fg="blue", cursor="hand2",
                                 justify="center")
                link1.pack(side="top")
                link1.bind("<Button-1>", lambda a, url="https://www.twitter.com/" + account: webbrowser.open_new(url))


        ## Case where no results were returned (e.g. username not found)
        else:
            label = tk.Label(self.resultsFrame,
                             text="No results found.\n Username is invalid or is not connected to an account with desired number of followers",
                             font=('Helvetica Neue', 14), background="#00AEF5", justify="center")
            label.pack(side="left", fill="x", expand=True)


class Node():
    def init(self, id=-1, name="", followers=-1, friends=-1, list_friends=[], num=-1):
        self.id = id
        self.num = num
        self.name = name
        self.followers = followers
        self.friends = friends
        self.list_friends = list_friends


class Graph():
    def __init__(self):
        self.nodes = []

    def insertnode(self, node):
        self.nodes.append(node)

    ##/////////////////////////////////////////////
    def findnode(self, username):
        for node in self.nodes:
            if node.name == username:
                return node

    def findid(self, id):
        for node in self.nodes:
            if node.id == id:
                return node

    def finalfunction(self, username, maxlayers, numfollowers, type):
        # Define  Variables we are going to use
        numoflayers = 1
        usernamelist = []
        followers = []
        visited = [False] * 1000
        empty = []
        # We get the node we wantg to work with
        node = self.findnode(username)
        # usernamelist.append(node.name)
        # followers.append(node.followers)
        # visited[node.num] = True #change

        if type == "BFS":  # BFS
            count = 0
            queue = []
            queue.append(node)
            while len(queue) != 0:

                current = queue.pop(0)

                if visited[current.num] == False:  # change
                    if numoflayers > int(maxlayers):
                        return usernamelist, followers
                    else:
                        numoflayers = numoflayers + 1
                        count = count + 1
                        usernamelist.append(current.name)
                        followers.append(current.followers)
                        visited[current.num] = True  # change
                        if current.followers > int(numfollowers) and count > 1:
                            return usernamelist, followers
                for friend in node.list_friends:

                    found_friend = self.findid(friend)
                    if found_friend is not None and visited[found_friend.num] == False:
                        print(found_friend.name)
                        queue.append(self.findid(friend))
            return usernamelist, followers
        else:  # DFS
            count = 0
            stack = []
            stack.append(node)
            while len(stack) != 0:
                current = stack.pop()
                if visited[current.num] == False:  # change
                    if numoflayers > int(maxlayers):
                        return usernamelist, followers
                    else:
                        numoflayers = numoflayers + 1
                        count = count + 1
                        usernamelist.append(current.name)
                        followers.append(current.followers)
                        visited[current.num] = True  # change
                        if current.followers > int(numfollowers) and count > 1:
                            return usernamelist, followers
                for friend in node.list_friends:
                    print(friend)
                    found_friend = self.findid(friend)
                    if found_friend is not None and visited[found_friend.num] == False:
                        stack.append(self.findid(friend))
            return usernamelist, followers


##/////////////////////////////////////////////
##----------------MAIN-------------------------
GraphObject = Graph()
df = pd.read_excel('SmallData.xlsx')
# nodelist = []
count = 0
for i in range(20):
    newnode = Node()
    newnode.id = df.loc[i][0]
    newnode.name = df.loc[i][1]
    newnode.followers = df.loc[i][4]
    newnode.friends = df.loc[i][5]
    newnode.num = count
    listFriends = []
    # Now we obtain the list of friends
    for j in range(newnode.friends):
        newid = df.loc[i][9 + j]
        if j != newnode.friends - 1:
            newid = newid[2: len(newid) - 1]
            listFriends.append(int(newid))

        elif j == newnode.friends - 1:
            newid = newid[2: (len(newid) - 1)]
            listFriends.append(int(newid))

    newnode.list_friends = listFriends
    # nodelist.append(newnode)
    GraphObject.insertnode(newnode)
    count = count + 1

guiObject = Gui()