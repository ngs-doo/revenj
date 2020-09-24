import { toast } from 'react-toastify';

export const notifyError = (message: string) => toast(message, { type: 'error' });
