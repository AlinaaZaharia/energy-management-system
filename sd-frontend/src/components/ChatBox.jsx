import React, { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../auth';

export default function ChatBox() {
    const { userId, username, role, token } = useAuth();
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([]);
    const [input, setInput] = useState('');
    const [stompClient, setStompClient] = useState(null);

    const [activeConversations, setActiveConversations] = useState({});
    const [selectedUser, setSelectedUser] = useState(null);
    const [typingInfo, setTypingInfo] = useState('');

    const messagesEndRef = useRef(null);
    const clientRef = useRef(null);

    useEffect(() => {
        if (!userId || !token) return;

        const socket = new SockJS('http://localhost/ws');
        const client = Stomp.over(socket);
        client.debug = () => {};

        client.connect({}, () => {
            console.log('✅ Chat Connected');

            if (role === 'ADMIN') {
                client.subscribe('/topic/admin/messages', (msg) => {
                    const body = JSON.parse(msg.body);
                    handleIncomingUserMessage(body);
                });

                client.subscribe('/topic/admin/typing', (msg) => {
                    const body = JSON.parse(msg.body);
                    setTypingInfo(`${body.senderName} is typing...`);
                    setTimeout(() => setTypingInfo(''), 2000);
                });

            } else {
                client.subscribe(`/topic/messages/${userId}`, (msg) => {
                    const body = JSON.parse(msg.body);
                    addMessageLocal(body.senderId, body.content, 'in', body.admin);
                });
            }
            setStompClient(client);
        }, (err) => console.error('Chat Error:', err));

        clientRef.current = client;

        return () => {
            if (clientRef.current) {
                try {
                    clientRef.current.disconnect(() => {
                        console.log("Chat disconnected gracefully");
                    });
                } catch (e) {
                }
            }
            if (socket && socket.readyState === 1) {
                socket.close();
            }
        };
    }, [userId, role, token]);

    function handleIncomingUserMessage(msg) {
        setActiveConversations(prev => {
            const userMsgs = prev[msg.senderId] ? [...prev[msg.senderId].msgs] : [];
            userMsgs.push({ text: msg.content, dir: 'in', sender: msg.senderName });

            return {
                ...prev,
                [msg.senderId]: { name: msg.senderName, msgs: userMsgs }
            };
        });

        if (!isOpen) setIsOpen(true);
        if (!selectedUser) setSelectedUser(msg.senderId);
    }

    function addMessageLocal(senderId, text, dir, isAdmin = false) {
        setMessages(prev => [...prev, { text, dir, sender: senderId, isAdmin }]);
    }

    function sendMessage(e) {
        e.preventDefault();
        if (!input.trim() || !stompClient) return;

        if (role === 'ADMIN') {
            if (!selectedUser) return;

            stompClient.send("/app/chat/admin", {}, JSON.stringify({
                senderId: "ADMIN",
                senderName: "Admin",
                content: input,
                admin: true,
                recipientId: selectedUser
            }));

            setActiveConversations(prev => {
                const userConv = prev[selectedUser] || { name: 'User', msgs: [] };
                const newMsgs = [...userConv.msgs, { text: input, dir: 'out' }];
                return { ...prev, [selectedUser]: { ...userConv, msgs: newMsgs } };
            });

        } else {
            stompClient.send("/app/chat", {}, JSON.stringify({
                senderId: userId,
                senderName: username,
                content: input,
                admin: false
            }));
            addMessageLocal(userId, input, 'out', false);
        }
        setInput('');
    }

    function handleTyping() {
        if (role === 'CLIENT' && stompClient) {
            stompClient.send("/app/chat/typing", {}, JSON.stringify({
                senderId: userId,
                senderName: username,
                admin: false
            }));
        }
    }

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, activeConversations, isOpen, selectedUser]);

    if (!token) return null;

    if (role === 'ADMIN') {
        const currentMsgs = selectedUser && activeConversations[selectedUser]
            ? activeConversations[selectedUser].msgs
            : [];

        return (
            <div style={{ ...styles.chatWindow, width: 650 }}>
                <div style={styles.header} onClick={() => setIsOpen(!isOpen)}>
                    Admin Support Panel {isOpen ? '▼' : '▲'}
                    {typingInfo && <span style={{fontSize:11, marginLeft:15, color:'#aaffaa', fontWeight:'normal'}}>{typingInfo}</span>}
                </div>
                {isOpen && (
                    <div style={{ display: 'flex', height: 350 }}>
                        <div style={{ width: 180, borderRight: '1px solid #444', overflowY: 'auto', background:'#111' }}>
                            <div style={{padding:8, fontSize:11, opacity:0.5, textTransform:'uppercase'}}>Active Chats</div>
                            {Object.keys(activeConversations).map(uid => (
                                <div key={uid} onClick={() => setSelectedUser(uid)}
                                     style={{
                                         padding: '10px', cursor: 'pointer', borderBottom:'1px solid #222',
                                         background: selectedUser === uid ? '#2a3a6b' : 'transparent',
                                         color: selectedUser === uid ? '#fff' : '#ccc'
                                     }}>
                                    <div style={{fontWeight:'bold', fontSize:13}}>{activeConversations[uid].name}</div>
                                    <div style={{fontSize:10, opacity:0.7}}>Click to reply</div>
                                </div>
                            ))}
                        </div>

                        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background:'#1a202c' }}>
                            <div style={styles.msgArea}>
                                {!selectedUser && <div style={{padding:20, opacity:0.5, textAlign:'center'}}>Select a user to start chatting</div>}
                                {selectedUser && currentMsgs.map((m, i) => (
                                    <div key={i} style={m.dir === 'out' ? styles.msgOut : styles.msgIn}>
                                        <div style={{fontSize:14}}>{m.text}</div>
                                    </div>
                                ))}
                                <div ref={messagesEndRef} />
                            </div>
                            <form onSubmit={sendMessage} style={styles.inputArea}>
                                <input style={styles.input} value={input} onChange={e => setInput(e.target.value)}
                                       placeholder="Type a reply..." disabled={!selectedUser} autoFocus />
                                <button style={{...styles.sendBtn, opacity: !selectedUser ? 0.5 : 1}} disabled={!selectedUser}>Send</button>
                            </form>
                        </div>
                    </div>
                )}
            </div>
        );
    }

    return (
        <div style={styles.chatWindow}>
            <div style={styles.header} onClick={() => setIsOpen(!isOpen)}>
                Chat Support {isOpen ? '▼' : '▲'}
            </div>
            {isOpen && (
                <>
                    <div style={styles.msgArea}>
                        {messages.length === 0 && (
                            <div style={{textAlign:'center', padding:20, opacity:0.6, fontSize:13}}>
                                Hello, <b>{username}</b>!<br/>I'm your automated assistant.<br/>
                                If I can't answer, a human agent will step in.
                            </div>
                        )}
                        {messages.map((m, i) => (
                            <div key={i} style={m.dir === 'out' ? styles.msgOut : styles.msgIn}>
                                <small style={{fontSize:9, opacity:0.7, marginBottom:2, display:'block'}}>
                                    {m.sender === 'SYSTEM' ? (m.isAdmin ? '👨‍💼 Support Agent' : '🤖 Bot') : 'You'}
                                </small>
                                <div>{m.text}</div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>
                    <form onSubmit={sendMessage} style={styles.inputArea}>
                        <input style={styles.input} value={input}
                               onChange={e => { setInput(e.target.value); handleTyping(); }}
                               placeholder="Type a message..." autoFocus />
                        <button style={styles.sendBtn}>➤</button>
                    </form>
                </>
            )}
        </div>
    );
}

const styles = {
    chatWindow: {
        position: 'fixed', bottom: 20, right: 20, width: 340,
        background: '#1a202c', borderRadius: '12px 12px 0 0',
        boxShadow: '0 -5px 25px rgba(0,0,0,0.5)', zIndex: 9000,
        border: '1px solid #2d3748', display: 'flex', flexDirection: 'column',
        fontFamily: 'system-ui, sans-serif'
    },
    header: {
        padding: '12px 16px', background: '#2b6cb0', color: 'white',
        fontWeight: 'bold', cursor: 'pointer', borderRadius: '12px 12px 0 0',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        borderBottom: '1px solid #2d3748'
    },
    msgArea: {
        height: 300, overflowY: 'auto', padding: 15, display: 'flex', flexDirection: 'column', gap: 10,
        background: '#171923'
    },
    msgIn: {
        alignSelf: 'flex-start', background: '#2d3748', color: '#e2e8f0',
        padding: '8px 12px', borderRadius: '12px 12px 12px 2px', maxWidth: '85%', fontSize: '14px',
        boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
    },
    msgOut: {
        alignSelf: 'flex-end', background: '#3182ce', color: 'white',
        padding: '8px 12px', borderRadius: '12px 12px 2px 12px', maxWidth: '85%', fontSize: '14px',
        boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
    },
    inputArea: {
        display: 'flex', borderTop: '1px solid #2d3748', padding: 10, background: '#1a202c'
    },
    input: {
        flex: 1, padding: '10px 14px', background: '#2d3748', border: 'none', borderRadius: '20px',
        color: 'white', outline: 'none', marginRight: 8, fontSize: '14px'
    },
    sendBtn: {
        background: '#2b6cb0', border: 'none', color: 'white', padding: '0',
        borderRadius: '50%', cursor: 'pointer', fontWeight: 'bold', width: '38px', height: '38px',
        display: 'flex', alignItems: 'center', justifyContent: 'center'
    }
};