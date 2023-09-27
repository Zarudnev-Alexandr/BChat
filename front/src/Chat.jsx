// Chat.js
import React, { useState, useEffect } from 'react';

function Chat() {
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [socket, setSocket] = useState(null);

  useEffect(() => {
    const newSocket = new WebSocket('ws://localhost:8000/ws');
    setSocket(newSocket);

    newSocket.onopen = () => {
      console.log('WebSocket is open now.');
    };

    newSocket.onmessage = (event) => {
      const message = event.data;
      setMessages([...messages, message]);
    };

    newSocket.onclose = () => {
      console.log('WebSocket is closed now.');
    };

    return () => {
      newSocket.close();
    };
  }, [messages]);

  const handleInputChange = (event) => {
    setInputMessage(event.target.value);
  };

  const sendMessage = () => {
    if (socket && inputMessage.trim() !== '') {
      socket.send(inputMessage);
      setInputMessage('');
    }
  };

  return (
    <div>
      <div>
        {messages.map((message, index) => (
          <div key={index}>{message}</div>
        ))}
      </div>
      <div>
        <input
          type="text"
          value={inputMessage}
          onChange={handleInputChange}
          placeholder="Введите сообщение..."
        />
        <button onClick={sendMessage}>Отправить</button>
      </div>
    </div>
  );
}

export default Chat;
