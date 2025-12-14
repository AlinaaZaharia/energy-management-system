import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useAuth } from '../auth';

export default function NotificationToast() {
    const { userId, token } = useAuth();
    const [notifications, setNotifications] = useState([]);

    useEffect(() => {
        if (!userId || !token) return;

        const stompClient = Stomp.over(() => new SockJS('http://localhost/ws'));

        stompClient.debug = () => {};

        stompClient.connect({}, () => {
            console.log('Connected to WebSocket for Notifications');

            stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
                if (message.body) {
                    const body = JSON.parse(message.body);
                    showNotification(body.message);
                }
            });
        }, (err) => {
            console.error('WebSocket Connection Error:', err);
        });

        return () => {
            if (stompClient.connected) stompClient.disconnect();
        };
    }, [userId, token]);

    function showNotification(msg) {
        const id = Date.now() + Math.random();
        setNotifications(prev => [...prev, { id, msg }]);

        setTimeout(() => setNotifications(prev => prev.filter(n => n.id !== id)), 6000);
    }

    if (notifications.length === 0) return null;

    return (
        <div style={styles.container}>
            {notifications.map(n => (
                <div key={n.id} style={styles.toast}>
                    <div style={styles.header}>Overconsumption Alert</div>
                    <div>{n.msg}</div>
                </div>
            ))}
        </div>
    );
}

const styles = {
    container: {
        position: 'fixed',
        top: 80,
        right: 20,
        zIndex: 9999,
        display: 'flex',
        flexDirection: 'column',
        gap: 10
    },
    toast: {
        background: 'rgba(220, 38, 38, 0.95)',
        color: '#fff',
        padding: '16px',
        borderRadius: '8px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        minWidth: '300px',
        maxWidth: '400px',
        borderLeft: '5px solid #7f1d1d',
        animation: 'slideIn 0.3s ease-out'
    },
    header: {
        fontWeight: 'bold',
        marginBottom: '4px',
        fontSize: '14px',
        textTransform: 'uppercase'
    }
};